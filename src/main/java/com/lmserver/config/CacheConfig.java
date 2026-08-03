package com.lmserver.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
/**
 * 本地缓存配置 — 基于 Caffeine，写入后60秒过期，最大1000条
 */

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    /** Caffeine 缓存管理器 — 60秒过期，最大1000条 */
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .maximumSize(1000));
        return manager;
    }
}
