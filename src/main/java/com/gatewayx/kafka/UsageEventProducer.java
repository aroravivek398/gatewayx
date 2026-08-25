package com.gatewayx.kafka;



import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UsageEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public UsageEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Long apiKeyId, String endpoint, int statusCode) {
        String message = String.format(
                "{\"apiKeyId\":%d,\"endpoint\":\"%s\",\"statusCode\":%d,\"occurredAt\":\"%s\"}",
                apiKeyId, endpoint, statusCode, LocalDateTime.now()
        );

        kafkaTemplate.send("usage-events", message);
    }
}