package com.gatewayx.ratelimit;

public interface RateLimiterStrategy {
    RateLimitResult tryConsume(String apiKeyId, int limitPerMinute);
}