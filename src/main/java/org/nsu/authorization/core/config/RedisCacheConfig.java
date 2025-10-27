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

    @Value("${spring.app.cache.verification-code.ttl-minutes}")
    private int ttl_minutes;

    private static final String VERIFICATION_CODE_CACHE = "Verification codes";

    @Value("${spring.app.cache.default-ttl-minutes}")
    private int defaultTtl;

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(defaultTtl))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    /**
     * Customizes the RedisCacheManager to set a specific TTL for the
     * "Verification codes" cache, overriding the default configuration.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        final Duration verificationCodeTtl = Duration.ofMinutes(ttl_minutes);

        return (builder) -> builder
                .withCacheConfiguration(
                        VERIFICATION_CODE_CACHE,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(verificationCodeTtl)
                                .disableCachingNullValues()
                                .serializeValuesWith(
                                        SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
    }

}