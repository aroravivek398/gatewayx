package com.gatewayx.controller;

import com.gatewayx.dto.request.CreateApiKeyRequest;
import com.gatewayx.dto.response.ApiKeyResponse;
import com.gatewayx.service.ApiKeyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {


    private final ApiKeyService apiKeyService;

    @PostMapping("/create")
    public ResponseEntity<ApiKeyResponse> create(@Valid @RequestBody CreateApiKeyRequest request ,HttpServletRequest httpServletRequest) {

        ApiKeyResponse response = apiKeyService.create((Long) httpServletRequest.getAttribute("developerId"), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-keys")
    public ResponseEntity<List<ApiKeyResponse>> list(HttpServletRequest request) {
        Long developerId = (Long) request.getAttribute("developerId");
        return ResponseEntity.ok(apiKeyService.listByDeveloper(developerId));
    }
    @PatchMapping("/{keyId}/revoke")
    public ResponseEntity<String> revoke(@PathVariable Long keyId,HttpServletRequest request) {
        apiKeyService.revoke(keyId, (Long)request.getAttribute("developerId"));
        String succesfull="API KEY SUCCESSFULLY REVOKED";
        return ResponseEntity.ok(succesfull);
    }
}
