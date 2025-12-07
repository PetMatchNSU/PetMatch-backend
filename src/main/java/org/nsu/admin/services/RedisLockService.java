package org.nsu.admin.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nsu.admin.dto.AdminModerationDto;
import org.nsu.admin.dto.LockModel;
import org.nsu.admin.dto.LockType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final RedisTemplate<Object, Object> redisTemplate;

    private final org.springframework.core.env.Environment env;

    private final Map<LockType, String> keyPrefixes = new HashMap<>();
    private final Map<LockType, Integer> ttlMinutes = new HashMap<>();

    @PostConstruct
    void initializeLockConfigurations() {
        for (LockType lockType : LockType.values()) {
            String keyPrefix = env.getRequiredProperty(lockType.getKeyPrefixConfigKey());
            Integer ttl = env.getRequiredProperty(lockType.getTtlMinutesConfigKey(), Integer.class);
            keyPrefixes.put(lockType, keyPrefix);
            ttlMinutes.put(lockType, ttl);
        }
    }

    public boolean setLock(Long id, Long moderatorId, LockType lockType) {
        String key = keyPrefixes.get(lockType) + id;

        LockModel existingLock = getLock(id, lockType);
        if (existingLock != null) {
            if (!existingLock.getModeratorId().equals(moderatorId)) {
                log.warn("Failed to set lock for {} - already locked by another moderator", id);
                return false;
            }
        }

        ZoneId zone = ZoneId.of(env.getProperty("app.timezone"));
        LocalDateTime now = LocalDateTime.now(zone);
        int ttl = ttlMinutes.get(lockType);
        LocalDateTime expiresAt = now.plusMinutes(ttl);

        LockModel lockInfo = new LockModel(id, moderatorId, now, expiresAt);

        redisTemplate.opsForValue().set(key, lockInfo, ttl, TimeUnit.MINUTES);
        log.info("Lock set/updated for {} by moderator {}", id, moderatorId);
        return true;
    }

    public LockModel getLock(Long id, LockType lockType) {
        String key = keyPrefixes.get(lockType) + id;
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof LockModel) {
            return (LockModel) value;
        }
        return null;
    }



    public boolean releaseLock(Long id, LockType lockType) {
        String key = keyPrefixes.get(lockType) + id;
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
