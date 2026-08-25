package com.gatewayx.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiKeyGeneratorTest {

    @Test
    void generateRawKey_shouldStartWithCorrectPrefix() {
        String rawKey = ApiKeyGenerator.generateRawKey();
        assertTrue(rawKey.startsWith("gwx_live_"));
    }

    @Test
    void generateRawKey_shouldGenerateUniqueKeysEachTime() {
        String key1 = ApiKeyGenerator.generateRawKey();
        String key2 = ApiKeyGenerator.generateRawKey();
        assertNotEquals(key1, key2);
    }

    @Test
    void hashKey_shouldProduceSameHashForSameInput() {
        String rawKey = "gwx_live_testkey123";
        String hash1 = ApiKeyGenerator.hashKey(rawKey);
        String hash2 = ApiKeyGenerator.hashKey(rawKey);
        assertEquals(hash1, hash2);
    }

    @Test
    void hashKey_shouldProduceDifferentHashForDifferentInput() {
        String hash1 = ApiKeyGenerator.hashKey("gwx_live_key1");
        String hash2 = ApiKeyGenerator.hashKey("gwx_live_key2");
        assertNotEquals(hash1, hash2);
    }
}