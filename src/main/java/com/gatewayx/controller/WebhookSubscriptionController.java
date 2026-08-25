package com.gatewayx.controller;

import com.gatewayx.dto.request.CreateWebhookSubscriptionRequest;
import com.gatewayx.dto.response.WebhookSubscriptionResponse;
import com.gatewayx.service.WebhookSubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookSubscriptionController {

    private final WebhookSubscriptionService webhookSubscriptionService;

    @PostMapping
    public ResponseEntity<WebhookSubscriptionResponse> create(
            @Valid @RequestBody CreateWebhookSubscriptionRequest request,
            HttpServletRequest httpServletRequest) {

        Long developerId = (Long) httpServletRequest.getAttribute("developerId");
        WebhookSubscriptionResponse response = webhookSubscriptionService.create(developerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}