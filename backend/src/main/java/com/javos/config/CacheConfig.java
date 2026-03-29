package com.javos.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_SYSTEM_CONFIG = "systemConfig";
    public static final String CACHE_SYSTEM_CONFIG_ALL = "systemConfigAll";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                CACHE_SYSTEM_CONFIG,
                CACHE_SYSTEM_CONFIG_ALL
        );
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500));
        return manager;
    }
}
