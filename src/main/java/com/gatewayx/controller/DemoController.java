package com.gatewayx.controller;

import com.gatewayx.idempotency.IdempotencyCheckResult;
import com.gatewayx.idempotency.IdempotencyService;
import com.gatewayx.idempotency.IdempotencyStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
public class DemoController {

    private final IdempotencyService idempotencyService;



    @GetMapping("/ping")
    public String ping() {
        return "pong - you are authenticated!";
    }
    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            HttpServletRequest request) {

        if (idempotencyKey == null) {
            return ResponseEntity.badRequest().body("{\"error\":\"IDEMPOTENCY_KEY_REQUIRED\"}");
        }

        IdempotencyCheckResult result = idempotencyService.checkAndClaim(idempotencyKey);

        if (result.status() == IdempotencyStatus.IN_PROGRESS) {
            return ResponseEntity.status(409).body("{\"error\":\"REQUEST_IN_PROGRESS\"}");
        }

        if (result.status() == IdempotencyStatus.COMPLETED) {
            return ResponseEntity.ok(result.cachedResponse());
        }

        // status is NEW — actually "do the work"
        String orderId = "ord_" + System.currentTimeMillis();
        String responseBody = "{\"orderId\":\"" + orderId + "\",\"status\":\"CREATED\"}";

        idempotencyService.markCompleted(idempotencyKey, responseBody);

        return ResponseEntity.ok(responseBody);
    }

}