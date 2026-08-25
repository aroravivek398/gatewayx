package com.gatewayx.idempotency;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class IdempotencyServiceTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private IdempotencyService idempotencyService;

    @Test
    void checkAndClaim_shouldAllowExactlyOneNewClaim_underConcurrentAccess() throws InterruptedException {
        String testKey = "concurrent-idempotency-key-" + System.currentTimeMillis();
        int totalThreads = 20;

        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch latch = new CountDownLatch(totalThreads);
        AtomicInteger newCount = new AtomicInteger(0);
        AtomicInteger inProgressCount = new AtomicInteger(0);

        for (int i = 0; i < totalThreads; i++) {
            executor.submit(() -> {
                try {
                    IdempotencyCheckResult result = idempotencyService.checkAndClaim(testKey);
                    if (result.status() == IdempotencyStatus.NEW) {
                        newCount.incrementAndGet();
                    } else if (result.status() == IdempotencyStatus.IN_PROGRESS) {
                        inProgressCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(1, newCount.get());
        assertEquals(totalThreads - 1, inProgressCount.get());
    }
}