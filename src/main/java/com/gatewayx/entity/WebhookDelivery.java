package com.gatewayx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_deliveries")
@Getter
@Setter
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long subscriptionId;

    private String eventType;

    private String payload;

    private Integer attemptCount = 0;

    @Enumerated(EnumType.STRING)
    private WebhookDeliveryStatus status;

    private LocalDateTime lastAttemptAt;

    private LocalDateTime nextRetryAt;
}