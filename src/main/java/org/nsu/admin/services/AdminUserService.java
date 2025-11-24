package org.nsu.admin.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nsu.admin.dto.*;
import org.nsu.admin.entity.StatusComment;
import org.nsu.admin.repositories.StatusCommentRepository;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.Status;
import org.nsu.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final StatusCommentRepository statusCommentRepository;
    private final RedisLockService redisLockService;

    public AdminUserListResponse getUsersList(AdminUserListRequest request) {
        // Variables
        int offset = 0;
        int limit = 20;
        Pageable pageable;
        List<String> statuses = null;
        String emailToken = null;
        Page<User> userPage;
        List<AdminUserDto> userDtos;
        AdminUserListResponse response;

        // Set defaults if pagination is null
        if (request.getPagination() != null) {
            offset = request.getPagination().getOffset() != null ? request.getPagination().getOffset() : 0;
            limit = request.getPagination().getLimit() != null ? request.getPagination().getLimit() : 20;
        }

        pageable = PageRequest.of(
            offset / limit,
            limit,
            Sort.by(Sort.Direction.DESC, "id")
        );

        if (request.getFilters() != null) {
            statuses = request.getFilters().getStatuses();
            emailToken = request.getFilters().getEmailToken();
        }

        if (statuses != null && !statuses.isEmpty()) {
            List<Status> statusEntities = statuses.stream()
                .map(statusName -> {
                    Status status = new Status();
                    status.setName(statusName);
                    return status;
                })
                .collect(Collectors.toList());

            if (emailToken != null && !emailToken.trim().isEmpty()) {
                userPage = userRepository.findByStatusInAndEmailContainingIgnoreCase(
                    statusEntities, emailToken.trim(), pageable);
            } else {
                userPage = userRepository.findByStatusIn(statusEntities, pageable);
            }
        } else {
            if (emailToken != null && !emailToken.trim().isEmpty()) {
                userPage = userRepository.findByEmailContainingIgnoreCase(emailToken.trim(), pageable);
            } else {
                userPage = userRepository.findAll(pageable);
            }
        }

        userDtos = userPage.getContent().stream()
            .map(this::convertToAdminUserDto)
            .collect(Collectors.toList());

        response = new AdminUserListResponse(userPage.getTotalElements(), userDtos);
        return response;
    }

    public boolean lockUserForModeration(Long userId) {
        Long moderatorId;
        boolean result;

        moderatorId = getCurrentModeratorId();
        result = redisLockService.setLock(userId, moderatorId);
        log.info("Moderator {} tried to lock user {} for moderation: {}", moderatorId, userId, result ? "success" : "failed - user already locked");
        return result;
    }

    @Transactional
    public void setUserStatus(Long userId, String targetStatus, String reason) {
        User user;
        Long moderatorId;
        UserLockModel lock;
        Status newStatus;
        StatusComment comment;

        user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Check if moderator owns the lock
        moderatorId = getCurrentModeratorId();
        lock = redisLockService.getLock(userId);
        if (lock == null || !lock.getModeratorId().equals(moderatorId)) {
            throw new RuntimeException("Moderator does not own the lock for user: " + userId);
        }

        // Update user status
        newStatus = new Status();
        newStatus.setName(targetStatus);
        user.setStatus(newStatus);
        userRepository.save(user);

        // Create status comment if provided
        if (reason != null && !reason.trim().isEmpty()) {
            comment = new StatusComment();
            comment.setUser(user);
            comment.setComment(reason.trim());
            comment.setDate(Timestamp.valueOf(LocalDateTime.now()));
            statusCommentRepository.save(comment);
        }

        // Release lock after status change
        redisLockService.releaseLock(userId);

        log.info("User {} status changed to {} by moderator {}", userId, targetStatus, moderatorId);
    }

    private AdminUserDto convertToAdminUserDto(User user) {
        AdminModerationDto moderation;

        moderation = redisLockService.getLockOld(user.getId());

        return new AdminUserDto(
            user.getId(),
            user.getStatus().getName(),
            user.getFirstName(),
            user.getSecondName(),
            user.getLastName(),
            user.getEmail(),
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
