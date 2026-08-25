package com.gatewayx.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class TokenBucketRateLimiterTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private TokenBucketRateLimiter tokenBucketRateLimiter;

    @Test
    void tryConsume_shouldAllowRequestsWithinLimit() {
        String testKey = "test-key-" + System.currentTimeMillis();
        RateLimitResult result = tokenBucketRateLimiter.tryConsume(testKey, 5);
        assertTrue(result.allowed());
    }

    @Test
    void tryConsume_shouldRejectRequestsExceedingLimit() {
        String testKey = "test-key-" + System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            tokenBucketRateLimiter.tryConsume(testKey, 5);
        }
        RateLimitResult result = tokenBucketRateLimiter.tryConsume(testKey, 5);
        assertFalse(result.allowed());
    }
    @Test
    void tryConsume_shouldAllowExactlyLimitRequests_underConcurrentAccess() throws InterruptedException {
        String testKey = "concurrent-test-key-" + System.currentTimeMillis();
        int limit = 10;
        int totalThreads = 30;

        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch latch = new CountDownLatch(totalThreads);
        AtomicInteger allowedCount = new AtomicInteger(0);

        for (int i = 0; i < totalThreads; i++) {
            executor.submit(() -> {
                try {
                    RateLimitResult result = tokenBucketRateLimiter.tryConsume(testKey, limit);
                    if (result.allowed()) {
                        allowedCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(limit, allowedCount.get());
    }
}