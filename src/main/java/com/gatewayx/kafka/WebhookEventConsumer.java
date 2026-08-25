package com.gatewayx.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatewayx.entity.WebhookDelivery;
import com.gatewayx.entity.WebhookDeliveryStatus;
import com.gatewayx.repository.WebhookDeliveryRepository;
import com.gatewayx.service.WebhookDeliveryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WebhookEventConsumer {

    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebhookDeliveryService webhookDeliveryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebhookEventConsumer(WebhookDeliveryRepository webhookDeliveryRepository,
                                WebhookDeliveryService webhookDeliveryService) {
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.webhookDeliveryService = webhookDeliveryService;
    }

    @KafkaListener(topics = "webhook-events", groupId = "gatewayx-webhook-consumer")
    public void consume(String message) throws Exception {
        Map<String, Object> data = objectMapper.readValue(message, Map.class);

        Long subscriptionId = Long.valueOf(data.get("subscriptionId").toString());
        String eventType = (String) data.get("eventType");
        Object payloadObj = data.get("payload");
        String payload = objectMapper.writeValueAsString(payloadObj);

        WebhookDelivery delivery = new WebhookDelivery();
        delivery.setSubscriptionId(subscriptionId);
        delivery.setEventType(eventType);
        delivery.setPayload(payload);
        delivery.setStatus(WebhookDeliveryStatus.PENDING);

        WebhookDelivery saved = webhookDeliveryRepository.save(delivery);

        webhookDeliveryService.attemptDelivery(saved);
    }
}