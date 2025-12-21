package org.nsu.admin.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nsu.admin.dto.*;
import org.nsu.admin.dto.LockType;
import org.nsu.admin.entity.StatusComment;
import org.nsu.admin.mappers.AdminUserMapper;
import org.nsu.admin.repositories.StatusCommentRepository;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.core.repositories.StatusRepository;
import org.nsu.users.core.services.TimezoneService;
import org.nsu.users.entity.Status;
import org.nsu.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService extends AdminServiceBase {

    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 20;

    private final UserRepository userRepository;
    private final StatusCommentRepository statusCommentRepository;
    private final RedisLockService redisLockService;
    private final StatusRepository statusRepository;
    private final AdminUserMapper adminUserMapper;
    private final TimezoneService timezoneService;

    public AdminUserListResponse getUsersList(AdminUserListRequest request) {
        // Variables
        int offset = DEFAULT_OFFSET;
        int limit = DEFAULT_LIMIT;
        Pageable pageable;
        List<String> statuses = null;
        String emailToken = null;
        Page<User> userPage;
        List<AdminUserDto> userDtos;
        AdminUserListResponse response;

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
            offset,
            limit,
            Sort.by(Sort.Direction.DESC, User.Fields.id)
        );

        if (request.getFilters() == null ||
            (CollectionUtils.isEmpty(request.getFilters().getStatuses()) &&
             !StringUtils.hasText(request.getFilters().getEmailToken()))) {
            // No filters - get all users
            userPage = userRepository.findAll(pageable);
        } else {
            if (request.getFilters() != null) {
                statuses = request.getFilters().getStatuses();
                emailToken = request.getFilters().getEmailToken();
            }

            // Normalize filters: treat empty list as null
            if (CollectionUtils.isEmpty(statuses)) {
                statuses = null;
            }
            if (StringUtils.hasText(emailToken)) {
                emailToken = emailToken.trim();
            } else {
                emailToken = null;
            }

            userPage = userRepository.findByFilters(statuses, emailToken, pageable);
        }

        userDtos = userPage.getContent().stream()
            .map(this::convertToAdminUserDto)
            .collect(Collectors.toList());

        response = new AdminUserListResponse(userDtos.size(), userDtos);
        return response;
    }

    public boolean lockUserForModeration(Long userId) {
        Long moderatorId;
        boolean result;

        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found: " + userId);
        }

        moderatorId = getCurrentModeratorId();
        result = redisLockService.setLock(userId, moderatorId, LockType.USER);
        log.info("Moderator {} tried to lock user {} for moderation: {}", moderatorId, userId, result ? "success" : "failed - user already locked");
        return result;
    }

    @Transactional
    public void setUserStatus(Long userId, String targetStatus, String reason) {
        User user;
        Long moderatorId;
        LockModel lock;
        Status newStatus;
        StatusComment comment;

        user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Check if moderator owns the lock
        moderatorId = getCurrentModeratorId();
        lock = redisLockService.getLock(userId, LockType.USER);
        if (lock == null || !lock.getModeratorId().equals(moderatorId)) {
            throw new RuntimeException("Moderator does not own the lock for user: " + userId);
        }

        // Update user status
        newStatus = statusRepository.findByName(targetStatus)
            .orElseThrow(() -> new RuntimeException("Status not found: " + targetStatus));
        user.setStatus(newStatus);
        userRepository.save(user);

        // Create status comment if provided
        if (StringUtils.hasText(reason)) {
            comment = new StatusComment();
            comment.setStatus(newStatus);
            comment.setUser(user);
            comment.setComment(reason.trim());
            comment.setDate(Timestamp.valueOf(timezoneService.now()));
            statusCommentRepository.save(comment);
        }

        // Release lock after status change
        redisLockService.releaseLock(userId, LockType.USER);

        log.info("User {} status changed to {} by moderator {}", userId, targetStatus, moderatorId);
    }

    private AdminUserDto convertToAdminUserDto(User user) {
        LockModel lock = redisLockService.getLock(user.getId(), LockType.USER);
        AdminModerationDto moderation = lock != null ?
            new AdminModerationDto(lock.getModeratorId(), lock.getLockedAt()) : null;
        return adminUserMapper.toDto(user, moderation);
    }
}
