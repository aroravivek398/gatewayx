package com.gatewayx.ratelimit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Qualifier("slidingWindow")
public class SlidingWindowRateLimiter implements RateLimiterStrategy {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> script;

    public SlidingWindowRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>();
        this.script.setScriptText(LUA_SCRIPT);
        this.script.setResultType(List.class);
    }

    private static final String LUA_SCRIPT = """
        local currentKey = KEYS[1]
        local previousKey = KEYS[2]
        local limit = tonumber(ARGV[1])
        local windowSizeSeconds = tonumber(ARGV[2])
        local nowMillis = tonumber(ARGV[3])

        local elapsedInWindow = nowMillis % (windowSizeSeconds * 1000)
        local weight = 1 - (elapsedInWindow / (windowSizeSeconds * 1000))

        local currentCount = tonumber(redis.call('GET', currentKey)) or 0
        local previousCount = tonumber(redis.call('GET', previousKey)) or 0

        local estimatedCount = (previousCount * weight) + currentCount

        local allowed = 0
        if estimatedCount < limit then
            redis.call('INCR', currentKey)
            redis.call('EXPIRE', currentKey, windowSizeSeconds * 2)
            allowed = 1
            estimatedCount = estimatedCount + 1
        end

        return {allowed, limit - estimatedCount}
        """;

    @Override
    public RateLimitResult tryConsume(String apiKeyId, int limitPerMinute) {
        long windowSizeSeconds = 60;
        long nowMillis = System.currentTimeMillis();
        long currentWindowStart = (nowMillis / 1000 / windowSizeSeconds) * windowSizeSeconds;
        long previousWindowStart = currentWindowStart - windowSizeSeconds;

        String currentKey = "ratelimit:window:" + apiKeyId + ":" + currentWindowStart;
        String previousKey = "ratelimit:window:" + apiKeyId + ":" + previousWindowStart;

        List<Long> result = redisTemplate.execute(
                script,
                java.util.Arrays.asList(currentKey, previousKey),
                String.valueOf(limitPerMinute),
                String.valueOf(windowSizeSeconds),
                String.valueOf(nowMillis)
        );

        boolean allowed = result.get(0) == 1;
        long remaining = Math.max(0, result.get(1));
        long retryAfter = allowed ? 0 : 1;

        return new RateLimitResult(allowed, remaining, retryAfter);
    }
}