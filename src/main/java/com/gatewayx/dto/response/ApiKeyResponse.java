package com.gatewayx.dto.response;

import com.gatewayx.entity.ApiKeyStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApiKeyResponse {

    private Long id;
    private String keyPrefix;
    private String rawKey;
    private String planName;
    private ApiKeyStatus status;
    private LocalDateTime createdAt;
}