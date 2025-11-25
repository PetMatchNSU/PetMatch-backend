package org.nsu.admin.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nsu.admin.dto.AdminModerationDto;
import org.nsu.admin.dto.LockModel;
import org.nsu.admin.dto.LockType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final RedisTemplate<Object, Object> redisTemplate;

    private final org.springframework.core.env.Environment env;

    private String getKeyPrefix(String type) {
        return env.getProperty("app.lock." + type + ".key-prefix");
    }

    private int getTtlMinutes(String type) {
        return env.getProperty("app.lock." + type + ".ttl-minutes", Integer.class, 5);
    }

    public boolean setLock(Long id, Long moderatorId, LockType lockType) {
        String type = lockType.name().toLowerCase();
        String key = getKeyPrefix(type) + id;

        LockModel existingLock = getLock(id, lockType);
        if (existingLock != null) {
            if (!existingLock.getModeratorId().equals(moderatorId)) {
                log.warn("Failed to set lock for {} - already locked by another moderator", id);
                return false;
            }
        }

        LocalDateTime now = LocalDateTime.now();
        int ttlMinutes = getTtlMinutes(type);
        LocalDateTime expiresAt = now.plusMinutes(ttlMinutes);

        LockModel lockInfo = new LockModel(id, moderatorId, now, expiresAt);

        redisTemplate.opsForValue().set(key, lockInfo, ttlMinutes, TimeUnit.MINUTES);
        log.info("Lock set/updated for {} by moderator {}", id, moderatorId);
        return true;
    }

    public LockModel getLock(Long id, LockType lockType) {
        String type = lockType.name().toLowerCase();
        String key = getKeyPrefix(type) + id;
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof LockModel) {
            return (LockModel) value;
        }
        return null;
    }

    // For backward compatibility - convert LockModel to AdminModerationDto
    public AdminModerationDto getLockOld(Long id, LockType lockType) {
        LockModel lock = getLock(id, lockType);
        if (lock != null) {
            return new AdminModerationDto(lock.getModeratorId(), lock.getLockedAt());
        }
        return null;
    }

    public boolean releaseLock(Long id, LockType lockType) {
        String type = lockType.name().toLowerCase();
        String key = getKeyPrefix(type) + id;
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("Lock released for {}", id);
            return true;
        } else {
            log.warn("Failed to release lock for {} - lock not found", id);
            return false;
        }
    }
}
