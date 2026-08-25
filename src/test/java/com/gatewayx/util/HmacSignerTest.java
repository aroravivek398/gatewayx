package com.gatewayx.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HmacSignerTest {

    @Test
    void sign_shouldProduceSameSignature_forSameInputAndSecret() {
        String signature1 = HmacSigner.sign("test-payload", "test-secret");
        String signature2 = HmacSigner.sign("test-payload", "test-secret");
        assertEquals(signature1, signature2);
    }

    @Test
    void sign_shouldProduceDifferentSignature_forDifferentPayload() {
        String signature1 = HmacSigner.sign("payload-1", "test-secret");
        String signature2 = HmacSigner.sign("payload-2", "test-secret");
        assertNotEquals(signature1, signature2);
    }

    @Test
    void sign_shouldProduceDifferentSignature_forDifferentSecret() {
        String signature1 = HmacSigner.sign("test-payload", "secret-1");
        String signature2 = HmacSigner.sign("test-payload", "secret-2");
        assertNotEquals(signature1, signature2);
    }
}