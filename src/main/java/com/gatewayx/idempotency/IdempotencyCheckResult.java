package com.gatewayx.idempotency;

public record IdempotencyCheckResult(IdempotencyStatus status,String cachedResponse) {
}
