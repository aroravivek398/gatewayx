package com.gatewayx.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatewayx.entity.ApiKey;
import com.gatewayx.entity.UsageAggregate;
import com.gatewayx.entity.UsageEvent;
import com.gatewayx.entity.WebhookSubscription;
import com.gatewayx.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsageEventConsumer {

    private final UsageEventRepository usageEventRepository;
    private final UsageAggregateRepository usageAggregateRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final WebhookSubscriptionRepository webhookSubscriptionRepository;
    private final WebhookEventProducer webhookEventProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "usage-events", groupId = "gatewayx-usage-consumer")
    public void consume(String message) throws Exception {
        Map<String, Object> data = objectMapper.readValue(message, Map.class);

        UsageEvent event = new UsageEvent();
        event.setApiKeyId(Long.valueOf(data.get("apiKeyId").toString()));
        event.setEndpoint((String) data.get("endpoint"));
        event.setStatusCode((Integer) data.get("statusCode"));
        event.setOccurredAt(LocalDateTime.parse((String) data.get("occurredAt")));

        usageEventRepository.save(event);

        Long apiKeyId = event.getApiKeyId();
        LocalDate periodStart = event.getOccurredAt().toLocalDate().withDayOfMonth(1);

        Optional<UsageAggregate> existing = usageAggregateRepository.findByApiKeyIdAndPeriodStart(apiKeyId, periodStart);

        long newCount;

        if (existing.isPresent()) {
            UsageAggregate aggregate = existing.get();
            aggregate.setRequestCount(aggregate.getRequestCount() + 1);
            usageAggregateRepository.save(aggregate);
            newCount = aggregate.getRequestCount();
        } else {
            UsageAggregate newAggregate = new UsageAggregate();
            newAggregate.setApiKeyId(apiKeyId);
            newAggregate.setPeriodStart(periodStart);
            newAggregate.setRequestCount(1L);
            usageAggregateRepository.save(newAggregate);
            newCount = 1L;
        }

        Optional<ApiKey> apiKeyOpt = apiKeyRepository.findById(apiKeyId);
        if (apiKeyOpt.isEmpty()) {
            return;
        }
        ApiKey apiKey = apiKeyOpt.get();

        long monthlyQuota = apiKey.getPlan().getMonthlyQuota();
        long threshold = (long) (monthlyQuota * 0.8);
        long previousCount = newCount - 1;

        if (previousCount < threshold && newCount >= threshold) {
            Long developerId = apiKey.getDeveloper().getId();

            List<WebhookSubscription> subscriptions = webhookSubscriptionRepository
                    .findByDeveloperIdAndEventTypesAndIsActiveTrue(developerId, "QUOTA_THRESHOLD_REACHED");

            for (WebhookSubscription subscription : subscriptions) {
                String payload = String.format(
                        "{\"event\":\"QUOTA_THRESHOLD_REACHED\",\"apiKeyId\":%d,\"requestCount\":%d,\"threshold\":%d}",
                        apiKeyId, newCount, threshold
                );
                webhookEventProducer.publish(subscription.getId(), "QUOTA_THRESHOLD_REACHED", payload);
            }
        }
    }
}