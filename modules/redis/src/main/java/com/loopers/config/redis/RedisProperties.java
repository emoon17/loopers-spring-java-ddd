package com.loopers.config.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Redis 설정 속성 클래스
 */
@ConfigurationProperties(value = "datasource.redis")
public record RedisProperties(
        int database,
        RedisNodeInfo master,
        List<RedisNodeInfo> replicas
) { }
