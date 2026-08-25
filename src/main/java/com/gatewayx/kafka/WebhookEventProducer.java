package com.gatewayx.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebhookEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public WebhookEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Long subscriptionId, String eventType, String payload) {
        String message = String.format(
                "{\"subscriptionId\":%d,\"eventType\":\"%s\",\"payload\":%s}",
                subscriptionId, eventType, payload
        );
        kafkaTemplate.send("webhook-events", message);
    }
}