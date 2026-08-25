package com.gatewayx.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WebhookSubscriptionResponse {

    private Long id;
    private String targetUrl;
    private String secret;
    private String eventTypes;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
