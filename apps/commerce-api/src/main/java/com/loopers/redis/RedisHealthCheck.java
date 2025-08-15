package com.loopers.redis;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthCheck implements CommandLineRunner {

    private final RedisConnectionFactory cf;

    public RedisHealthCheck(RedisConnectionFactory cf) {
        this.cf = cf;
    }
    @Override
    public void run(String... args) throws Exception {
        try (RedisConnection conn =  cf.getConnection()){
            String pong = conn.ping();
            System.out.println("[redis] ping ok ::: " + pong);
        } catch (Exception e) {
            System.out.println("[redis] ping error ::: " + e.getMessage());
        }
    }
}
