package com.gatewayx.security;

import com.gatewayx.entity.ApiKey;
import com.gatewayx.entity.UsageAggregate;
import com.gatewayx.kafka.UsageEventProducer;
import com.gatewayx.ratelimit.RateLimitResult;
import com.gatewayx.ratelimit.RateLimiterStrategy;
import com.gatewayx.repository.ApiKeyRepository;
import com.gatewayx.repository.UsageAggregateRepository;
import com.gatewayx.util.ApiKeyGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static com.gatewayx.entity.ApiKeyStatus.REVOKED;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final UsageEventProducer usageEventProducer;
    private final RateLimiterStrategy rateLimiterStrategy;
    private final UsageAggregateRepository usageAggregateRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.startsWith("/api/v1/demo")) {
            filterChain.doFilter(request, response);
            return;
        }

        String rawKey = request.getHeader("X-API-Key");
        if (rawKey == null) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"MISSING_API_KEY\"}");
            return;
        }

        String hashedKey = ApiKeyGenerator.hashKey(rawKey);
        Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByKeyHash(hashedKey);

        if (apiKeyOpt.isEmpty() || apiKeyOpt.get().getStatus() == REVOKED) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"INVALID_API_KEY\"}");
            return;
        }

        ApiKey apiKey = apiKeyOpt.get();
        int limit = apiKey.getPlan().getRateLimitPerMinute();
        RateLimitResult result = rateLimiterStrategy.tryConsume(String.valueOf(apiKey.getId()), limit);
        if (!result.allowed()) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"RATE_LIMIT_EXCEEDED\",\"retryAfterSeconds\":" + result.retryAfterSeconds() + "}"
            );
            return;
        }

        LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
        Optional<UsageAggregate> aggregateOpt = usageAggregateRepository
                .findByApiKeyIdAndPeriodStart(apiKey.getId(), periodStart);

        if (aggregateOpt.isPresent()) {
            long currentUsage = aggregateOpt.get().getRequestCount();
            long quota = apiKey.getPlan().getMonthlyQuota();
            if (currentUsage >= quota) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"QUOTA_EXCEEDED\"}");
                return;
            }
        }

        usageEventProducer.publish(apiKey.getId(), path, 200);
        filterChain.doFilter(request, response);
    }
}