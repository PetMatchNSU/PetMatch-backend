package org.nsu.admin.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nsu.admin.dto.*;
import org.nsu.admin.dto.AdminModerationDto;
import org.nsu.admin.dto.AdminModerationDto;
import org.nsu.admin.dto.LockType;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardStatus;
import org.nsu.animal.repository.AnimalCardRepository;
import org.nsu.animal.repository.AnimalCardStatusRepository;
import org.nsu.authorization.core.security.PersonDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCardService {

    private final AnimalCardRepository animalCardRepository;
    private final AnimalCardStatusRepository animalCardStatusRepository;
    private final RedisLockService redisLockService;

    public AdminCardListResponse getCardsList(AdminCardListRequest request) {
        // Variables
        int offset = 0;
        int limit = 20;
        Pageable pageable;
        List<String> statuses = null;
        List<String> goals = null;
        LocalDate createdAt = null;
        LocalDate updatedAt = null;
        Page<AnimalCard> cardPage;
        List<AdminCardDto> cardDtos;
        AdminCardListResponse response;

        // Set defaults if pagination is null
        if (request.getPagination() != null) {
            offset = request.getPagination().getOffset() != null ? request.getPagination().getOffset() : 0;
            limit = request.getPagination().getLimit() != null ? request.getPagination().getLimit() : 20;
        }

        pageable = PageRequest.of(
            offset / limit,
            limit,
            Sort.by(Sort.Direction.ASC, "created")
        );

        // Extract filters from request
        AdminCardFilters filters = request.getFilters();
        if (filters == null || (filters.getStatuses() == null && filters.getGoals() == null &&
                                filters.getCreatedAt() == null && filters.getUpdatedAt() == null)) {
            // No filters - get all cards
            cardPage = animalCardRepository.findAll(pageable);
        } else {
            statuses = filters.getStatuses();
            goals = filters.getGoals();
            createdAt = filters.getCreatedAt();
            updatedAt = filters.getUpdatedAt();

            cardPage = animalCardRepository.findByFilters(statuses, goals, createdAt, updatedAt, pageable);
        }

        cardDtos = cardPage.getContent().stream()
            .map(this::convertToAdminCardDto)
            .collect(Collectors.toList());

        response = new AdminCardListResponse(cardPage.getTotalElements(), cardDtos);
        return response;
    }

    public boolean lockCardForModeration(Long cardId) {
        Long moderatorId;
        boolean result;

        moderatorId = getCurrentModeratorId();
        result = redisLockService.setLock(cardId, moderatorId, LockType.CARD);
        log.info("Moderator {} tried to lock card {} for moderation: {}", moderatorId, cardId, result ? "success" : "failed - card already locked");
        return result;
    }

    @Transactional
    public void setCardStatus(Long cardId, String targetStatus, String reason) {
        AnimalCard card;
        Long moderatorId;
        LockModel lock;
        AnimalCardStatus newCardStatus;

        card = animalCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found: " + cardId));

        // Check if moderator owns the lock
        moderatorId = getCurrentModeratorId();
        lock = redisLockService.getLock(cardId, LockType.CARD);
        if (lock == null || !lock.getModeratorId().equals(moderatorId)) {
            throw new RuntimeException("Moderator does not own the lock for card: " + cardId);
        }

        // Update card status
        newCardStatus = animalCardStatusRepository.findByName(targetStatus)
            .orElseThrow(() -> new RuntimeException("Status not found: " + targetStatus));
        card.setStatus(newCardStatus);
        animalCardRepository.save(card);

        // Conflict: StatusComment содержит поле типа Status, но такой вариант подходит для пользователя, а животные имеют поле AnimalCardStatus

        // Release lock after status change
        redisLockService.releaseLock(cardId, LockType.CARD);

        log.info("Card {} status changed to {} by moderator {}", cardId, targetStatus, moderatorId);
    }

    private AdminCardDto convertToAdminCardDto(AnimalCard card) {
        AdminModerationDto moderation;

        moderation = redisLockService.getLockOld(card.getId(), LockType.CARD);

        return new AdminCardDto(
            card.getId(),
            card.getAnimal().getId(),
            card.getStatus().getName(),
            card.getGoal().getGoal(),
            card.getName(),
            card.getCreated().toLocalDate(),
            card.getUpdated().toLocalDate(),
            new AdminCardOwnerDto(card.getCardAuthor().getId(),
                                 card.getCardAuthor().getFirstName(),
                                 card.getCardAuthor().getSecondName(),
                                 card.getCardAuthor().getLastName()),
            moderation
        );
    }

    private Long getCurrentModeratorId() {
        Authentication authentication;
        PersonDetails personDetails;

        authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof PersonDetails) {
            personDetails = (PersonDetails) authentication.getPrincipal();
            return personDetails.getUserId();
        }
        throw new RuntimeException("Unable to get current moderator ID from security context");
    }
}
