package com.gatewayx.idempotency;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final Duration TTL = Duration.ofHours(24);

    public IdempotencyCheckResult checkAndClaim(String idempotencyKey) {
        String redisKey = "idempotency:" + idempotencyKey;

        Boolean claimed = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, IN_PROGRESS, TTL);

        if (Boolean.TRUE.equals(claimed)) {
            return new IdempotencyCheckResult(IdempotencyStatus.NEW, null);
        }

        String existingValue = redisTemplate.opsForValue().get(redisKey);

        if (existingValue == null) {
            // extremely rare edge case: key expired between the failed claim and this read
            return new IdempotencyCheckResult(IdempotencyStatus.NEW, null);
        }

        if (existingValue.equals(IN_PROGRESS)) {
            return new IdempotencyCheckResult(IdempotencyStatus.IN_PROGRESS, null);
        }

        // otherwise, it's a completed response stored as "COMPLETED:<the actual response body>"
        String cachedResponse = existingValue.substring("COMPLETED:".length());
        return new IdempotencyCheckResult(IdempotencyStatus.COMPLETED, cachedResponse);
    }

    public void markCompleted(String idempotencyKey, String responseBody) {
        String redisKey = "idempotency:" + idempotencyKey;
        redisTemplate.opsForValue().set(redisKey, "COMPLETED:" + responseBody, TTL);
    }
}