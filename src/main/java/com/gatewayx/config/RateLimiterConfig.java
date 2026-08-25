package com.gatewayx.config;

import com.gatewayx.ratelimit.RateLimiterStrategy;
import com.gatewayx.ratelimit.SlidingWindowRateLimiter;
import com.gatewayx.ratelimit.TokenBucketRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RateLimiterConfig {

    @Value("${gatewayx.ratelimit.strategy}")
    private String strategy;

    private final TokenBucketRateLimiter tokenBucketRateLimiter;
    private final SlidingWindowRateLimiter slidingWindowRateLimiter;

    @Bean
    public RateLimiterStrategy rateLimiterStrategy() {
        if (strategy.equals("slidingWindow")) {
            return slidingWindowRateLimiter;
        }
        return tokenBucketRateLimiter;
    }
}