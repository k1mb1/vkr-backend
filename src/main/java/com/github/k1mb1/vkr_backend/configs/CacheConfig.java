package com.github.k1mb1.vkr_backend.configs;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Кэш снапшотов прав пользователя. TTL намеренно короткий: снимает дубли запросов в
 * пределах обработки одного HTTP-запроса (и быстрых серий), но не «залипает» — а на
 * любое изменение прав срабатывает {@code @CacheEvict} в сервисе прав, так что
 * пользователь не ходит со стухшими правами.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String USER_PERMISSIONS_CACHE = "userPermissions";

    @Bean
    public CacheManager cacheManager() {
        var cacheManager = new CaffeineCacheManager(USER_PERMISSIONS_CACHE);
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(60))
                .maximumSize(10_000)
        );
        return cacheManager;
    }
}
