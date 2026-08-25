package com.gatewayx.service;


import com.gatewayx.dto.request.CreateWebhookSubscriptionRequest;
import com.gatewayx.dto.response.WebhookSubscriptionResponse;
import com.gatewayx.entity.WebhookSubscription;
import com.gatewayx.repository.WebhookSubscriptionRepository;
import com.gatewayx.util.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WebhookSubscriptionService {
    private final WebhookSubscriptionRepository webhookSubscriptionRepository;

    public WebhookSubscriptionResponse create(Long developerId, CreateWebhookSubscriptionRequest request){
        WebhookSubscription webhookSubscription = new WebhookSubscription();
        webhookSubscription.setDeveloperId(developerId);
        webhookSubscription.setTargetUrl(request.getTargetUrl());
        webhookSubscription.setEventTypes(request.getEventTypes());
        String secret = ApiKeyGenerator.generateWebhookSecret();
        webhookSubscription.setSecret(secret);
        webhookSubscriptionRepository.save(webhookSubscription);
        WebhookSubscriptionResponse webhookSubscriptionResponse = new WebhookSubscriptionResponse();

        webhookSubscriptionResponse.setId(webhookSubscription.getId());
        webhookSubscriptionResponse.setSecret(webhookSubscription.getSecret());
        webhookSubscriptionResponse.setTargetUrl(webhookSubscription.getTargetUrl());
        webhookSubscriptionResponse.setEventTypes(webhookSubscription.getEventTypes());
        webhookSubscriptionResponse.setIsActive(webhookSubscription.getIsActive());
        webhookSubscriptionResponse.setCreatedAt(webhookSubscription.getCreatedAt());
        return  webhookSubscriptionResponse;
    }
}
