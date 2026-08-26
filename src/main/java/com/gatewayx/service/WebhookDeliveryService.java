package com.gatewayx.service;

import com.gatewayx.entity.WebhookDelivery;
import com.gatewayx.entity.WebhookDeliveryStatus;
import com.gatewayx.entity.WebhookSubscription;
import com.gatewayx.repository.WebhookDeliveryRepository;
import com.gatewayx.repository.WebhookSubscriptionRepository;
import com.gatewayx.util.HmacSigner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WebhookDeliveryService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long[] BACKOFF_MINUTES = {1, 4, 16, 60, 60};

    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebhookSubscriptionRepository webhookSubscriptionRepository;
    private final RestClient restClient;

    public WebhookDeliveryService(WebhookDeliveryRepository webhookDeliveryRepository,
                                  WebhookSubscriptionRepository webhookSubscriptionRepository) {
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.webhookSubscriptionRepository = webhookSubscriptionRepository;
        this.restClient = RestClient.create();
    }

    public void attemptDelivery(WebhookDelivery delivery) {
        WebhookSubscription subscription = webhookSubscriptionRepository
                .findById(delivery.getSubscriptionId())
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        String signature = HmacSigner.sign(delivery.getPayload(), subscription.getSecret());

        try {
            restClient.post()
                    .uri(subscription.getTargetUrl())
                    .header("X-GatewayX-Signature", signature)
                    .header("Content-Type", "application/json")
                    .body(delivery.getPayload())
                    .retrieve()
                    .toBodilessEntity();

            delivery.setStatus(WebhookDeliveryStatus.DELIVERED);
            delivery.setAttemptCount(delivery.getAttemptCount() + 1);
            delivery.setLastAttemptAt(LocalDateTime.now());
            webhookDeliveryRepository.save(delivery);

        } catch (Exception e) {
            handleFailedAttempt(delivery);
        }
    }

    private void handleFailedAttempt(WebhookDelivery delivery) {
        int newAttemptCount = delivery.getAttemptCount() + 1;
        delivery.setAttemptCount(newAttemptCount);
        delivery.setLastAttemptAt(LocalDateTime.now());

        if (newAttemptCount >= MAX_ATTEMPTS) {
            delivery.setStatus(WebhookDeliveryStatus.FAILED);
            delivery.setNextRetryAt(null);
        } else {
            long delayMinutes = BACKOFF_MINUTES[newAttemptCount - 1];
            delivery.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
        }

        webhookDeliveryRepository.save(delivery);
    }
    @Scheduled(fixedRate = 30000)
    public void retryDueDeliveries() {
        List<WebhookDelivery> dueDeliveries = webhookDeliveryRepository
                .findByStatusAndNextRetryAtBefore(WebhookDeliveryStatus.PENDING, LocalDateTime.now());

        for (WebhookDelivery delivery : dueDeliveries) {
            attemptDelivery(delivery);
        }
    }
}