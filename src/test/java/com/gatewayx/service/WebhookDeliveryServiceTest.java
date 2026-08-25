package com.gatewayx.service;

import com.gatewayx.entity.WebhookDelivery;
import com.gatewayx.entity.WebhookDeliveryStatus;
import com.gatewayx.entity.WebhookSubscription;
import com.gatewayx.repository.WebhookDeliveryRepository;
import com.gatewayx.repository.WebhookSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookDeliveryServiceTest {

    @Mock
    private WebhookDeliveryRepository webhookDeliveryRepository;

    @Mock
    private WebhookSubscriptionRepository webhookSubscriptionRepository;

    @InjectMocks
    private WebhookDeliveryService webhookDeliveryService;

    @Test
    void handleFailedAttempt_shouldScheduleRetry_whenUnderMaxAttempts() {
        WebhookSubscription subscription = new WebhookSubscription();
        subscription.setId(1L);
        subscription.setTargetUrl("http://this-will-fail-invalid-url");
        subscription.setSecret("test-secret");

        WebhookDelivery delivery = new WebhookDelivery();
        delivery.setSubscriptionId(1L);
        delivery.setPayload("{\"event\":\"test\"}");
        delivery.setAttemptCount(0);
        delivery.setStatus(WebhookDeliveryStatus.PENDING);

        when(webhookSubscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(webhookDeliveryRepository.save(any(WebhookDelivery.class))).thenReturn(delivery);

        webhookDeliveryService.attemptDelivery(delivery);

        assertEquals(1, delivery.getAttemptCount());
        assertEquals(WebhookDeliveryStatus.PENDING, delivery.getStatus());
        assertNotNull(delivery.getNextRetryAt());
    }

    @Test
    void attemptDelivery_shouldMarkFailed_whenMaxAttemptsReached() {
        WebhookSubscription subscription = new WebhookSubscription();
        subscription.setId(1L);
        subscription.setTargetUrl("http://this-will-fail-invalid-url");
        subscription.setSecret("test-secret");

        WebhookDelivery delivery = new WebhookDelivery();
        delivery.setSubscriptionId(1L);
        delivery.setPayload("{\"event\":\"test\"}");
        delivery.setAttemptCount(4);
        delivery.setStatus(WebhookDeliveryStatus.PENDING);

        when(webhookSubscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(webhookDeliveryRepository.save(any(WebhookDelivery.class))).thenReturn(delivery);

        webhookDeliveryService.attemptDelivery(delivery);

        assertEquals(5, delivery.getAttemptCount());
        assertEquals(WebhookDeliveryStatus.FAILED, delivery.getStatus());
        assertNull(delivery.getNextRetryAt());
    }
}