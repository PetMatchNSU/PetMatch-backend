package org.nsu.admin.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.nsu.admin.dto.*;
import org.nsu.admin.entity.StatusComment;
import org.nsu.admin.mappers.AdminCardMapper;
import org.nsu.admin.repositories.StatusCommentRepository;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardStatus;
import org.nsu.animal.repository.AnimalCardRepository;
import org.nsu.animal.repository.AnimalCardStatusRepository;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCardService extends AdminServiceBase {

    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 20;

    private final AnimalCardRepository animalCardRepository;
    private final AnimalCardStatusRepository animalCardStatusRepository;
    private final StatusCommentRepository statusCommentRepository;
    private final UserRepository userRepository;
    private final RedisLockService redisLockService;
    private final AdminCardMapper adminCardMapper;

    public AdminCardListResponse getCardsList(AdminCardListRequest request) {
        // Variables
        int offset = DEFAULT_OFFSET;
        int limit = DEFAULT_LIMIT;
        Pageable pageable;
        List<String> statuses = null;
        List<String> goals = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;
        Page<AnimalCard> cardPage;
        List<AdminCardDto> cardDtos;
        AdminCardListResponse response;

        // Set defaults if pagination is null
        if (request.getPagination() != null) {
            offset = Optional.ofNullable(request.getPagination().getOffset()).orElse(DEFAULT_OFFSET);
            limit = Optional.ofNullable(request.getPagination().getLimit()).orElse(DEFAULT_LIMIT);
        }

        // Prevent division by zero
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }

        pageable = PageRequest.of(
            offset / limit,
            limit,
            Sort.by(Sort.Direction.ASC, AnimalCard.Fields.created)
        );

        // Extract filters from request
        AdminCardFilters filters = request.getFilters();

        if (filters == null || (CollectionUtils.isEmpty(filters.getStatuses()) &&
                                CollectionUtils.isEmpty(filters.getGoals()) &&
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

        // Check if card exists
        if (!animalCardRepository.existsById(cardId)) {
            throw new RuntimeException("Card not found: " + cardId);
        }

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
            .orElseThrow(() -> new RuntimeException("Card not found1: " + cardId));

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

        // Create status comment
        User moderator = userRepository.findById(moderatorId)
            .orElseThrow(() -> new RuntimeException("Moderator not found: " + moderatorId));

        StatusComment statusComment = new StatusComment();
        statusComment.setAnimalCardStatus(newCardStatus);
        statusComment.setAnimalCard(card);
        statusComment.setComment(reason);
        statusComment.setDate(new Timestamp(System.currentTimeMillis()));
        statusCommentRepository.save(statusComment);

        // Release lock after status change
        redisLockService.releaseLock(cardId, LockType.CARD);

        log.info("Card {} status changed to {} by moderator {}", cardId, targetStatus, moderatorId);
    }

    private AdminCardDto convertToAdminCardDto(AnimalCard card) {
        LockModel lock = redisLockService.getLock(card.getId(), LockType.CARD);
        AdminModerationDto moderation = lock != null ?
            new AdminModerationDto(lock.getModeratorId(), lock.getLockedAt()) : null;
        return adminCardMapper.toDto(card, moderation);
    }
}
