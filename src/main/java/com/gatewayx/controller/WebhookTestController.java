package com.gatewayx.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook-test")
public class WebhookTestController {

    @PostMapping("/receiver")
    public ResponseEntity<String> receive(
            @RequestHeader(value = "X-GatewayX-Signature", required = false) String signature,
            @RequestBody String payload) {

        System.out.println("Webhook received!");
        System.out.println("Signature: " + signature);
        System.out.println("Payload: " + payload);

        return ResponseEntity.ok("received");
    }
}