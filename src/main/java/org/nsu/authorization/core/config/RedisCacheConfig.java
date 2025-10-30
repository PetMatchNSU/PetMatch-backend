package org.nsu.authorization.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import java.time.Duration;

@Configuration
public class RedisCacheConfig {

    @Value("${spring.data.redis.verification-code.ttl-minutes}")
    private int ttl_minutes;

    private static final String VERIFICATION_CODE_CACHE = "Verification codes";

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                // Set a sensible default TTL (e.g., no TTL or a long one)
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    /**
     * Customizes the RedisCacheManager to set a specific TTL for the
     * "Verification codes" cache, overriding the default configuration.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        // Calculate the Duration using the injected value
        final Duration verificationCodeTtl = Duration.ofMinutes(ttl_minutes);

        return (builder) -> builder
                .withCacheConfiguration(
                        VERIFICATION_CODE_CACHE,
                        // Create a specific config for this cache, inheriting from the default,
                        // but overriding the TTL.
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(verificationCodeTtl)
                                .disableCachingNullValues()
                                .serializeValuesWith(
                                        SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
    }

}
