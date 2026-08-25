package com.gatewayx.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApiKeyRequest {

    @NotNull
    private Long planId;
}