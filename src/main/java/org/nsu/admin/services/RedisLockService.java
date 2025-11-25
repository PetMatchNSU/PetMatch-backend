package org.nsu.admin.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nsu.admin.dto.AdminModerationDto;
import org.nsu.admin.dto.UserLockModel;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final RedisTemplate<Object, Object> redisTemplate;

    private static final String LOCK_KEY_PREFIX = "moderation:lock:user:";

    // TTL for lock expiration in minutes
    private static final int LOCK_TTL_MINUTES = 30;

    public boolean setLock(Long userId, Long moderatorId) {
        String key = LOCK_KEY_PREFIX + userId;

        UserLockModel existingLock = getLock(userId);
        if (existingLock != null) {
            if (!existingLock.getModeratorId().equals(moderatorId)) {
                log.warn("Failed to set lock for user {} - already locked by another moderator", userId);
                return false;
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(LOCK_TTL_MINUTES);

        UserLockModel lockInfo = new UserLockModel(userId, moderatorId, now, expiresAt);

        redisTemplate.opsForValue().set(key, lockInfo, LOCK_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("Lock set/updated for user {} by moderator {}", userId, moderatorId);
        return true;
    }

    public UserLockModel getLock(Long userId) {
        String key = LOCK_KEY_PREFIX + userId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof UserLockModel) {
            return (UserLockModel) value;
        }
        return null;
    }

    // For backward compatibility - convert UserLockModel to AdminModerationDto
    public AdminModerationDto getLockOld(Long userId) {
        UserLockModel lock = getLock(userId);
        if (lock != null) {
            return new AdminModerationDto(lock.getModeratorId(), lock.getLockedAt());
        }
        return null;
    }

    public boolean releaseLock(Long userId) {
        String key = LOCK_KEY_PREFIX + userId;
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("Lock released for user {}", userId);
            return true;
        } else {
            log.warn("Failed to release lock for user {} - lock not found", userId);
            return false;
        }
    }
}
