package com.gatewayx.ratelimit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Qualifier("tokenBucket")
public class TokenBucketRateLimiter implements RateLimiterStrategy {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> script;

    public TokenBucketRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>();
        this.script.setScriptText(LUA_SCRIPT);
        this.script.setResultType(List.class);
    }

    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])
        local refillPerSecond = tonumber(ARGV[2])
        local now = tonumber(ARGV[3])

        local bucket = redis.call('HMGET', key, 'tokens', 'lastRefill')
        local tokens = tonumber(bucket[1])
        local lastRefill = tonumber(bucket[2])

        if tokens == nil then
            tokens = capacity
            lastRefill = now
        end

        local elapsed = math.max(0, now - lastRefill)
        local refillAmount = elapsed * refillPerSecond
        tokens = math.min(capacity, tokens + refillAmount)

        local allowed = 0
        if tokens >= 1 then
            tokens = tokens - 1
            allowed = 1
        end

        redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', now)
        redis.call('EXPIRE', key, 3600)

        return {allowed, tokens}
        """;

    @Override
    public RateLimitResult tryConsume(String apiKeyId, int limitPerMinute) {
        String key = "ratelimit:bucket:" + apiKeyId;
        double refillPerSecond = limitPerMinute / 60.0;
        long now = System.currentTimeMillis() / 1000;

        List<Long> result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(limitPerMinute),
                String.valueOf(refillPerSecond),
                String.valueOf(now)
        );

        boolean allowed = result.get(0) == 1;
        long remaining = result.get(1);
        long retryAfter = allowed ? 0 : 1;

        return new RateLimitResult(allowed, remaining, retryAfter);
    }
}