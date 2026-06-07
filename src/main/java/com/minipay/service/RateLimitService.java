package com.minipay.service;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean allow(String key, int limit, Duration window) {
        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount != null && currentCount == 1L) {
            redisTemplate.expire(key, window);
        }
        return currentCount != null && currentCount <= limit;
    }
}
