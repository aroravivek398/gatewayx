package com.gatewayx.repository;

import com.gatewayx.entity.WebhookDelivery;
import com.gatewayx.entity.WebhookDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {
    List<WebhookDelivery> findByStatusAndNextRetryAtBefore(WebhookDeliveryStatus status, LocalDateTime time);
}