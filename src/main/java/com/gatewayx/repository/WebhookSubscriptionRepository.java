package com.gatewayx.repository;

import com.gatewayx.entity.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Long> {
    List<WebhookSubscription> findByDeveloperIdAndEventTypesAndIsActiveTrue(Long developerId, String eventTypes);
}