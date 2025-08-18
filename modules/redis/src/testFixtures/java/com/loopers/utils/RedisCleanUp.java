package com.loopers.utils;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;


/**
 * Redis 테스트 데이터 제거 유틸
 */
@Component
public class RedisCleanUp {
    private final RedisConnectionFactory redisConnectionFactory;

    public RedisCleanUp(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public void truncateAll(){
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.serverCommands().flushAll();
        }
    }
}
