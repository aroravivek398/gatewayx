package com.gatewayx.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class ApiKeyGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateRawKey() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return "gwx_live_" + randomPart;
    }

    public static String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }
    public static String generateWebhookSecret() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return "whsec_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
