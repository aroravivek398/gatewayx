package com.gatewayx.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWebhookSubscriptionRequest {

    @NotBlank
    private String targetUrl;

    @NotBlank
    private String eventTypes;
}