package com.hexaware.fastx.service;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests for Razorpay integration helpers.
 * NOTE: These are unit tests only — no real Razorpay API calls are made.
 * The actual payment flow is tested manually / via Razorpay test mode.
 */
@SpringBootTest
@ActiveProfiles("test")
class RazorpayServiceTest {

    // ── HMAC-SHA256 signature utility (mirrors RazorpayController.hmacSha256) ──
    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Test
    void testHmacSignatureIsConsistent() throws Exception {
        String payload = "order_TEST123|pay_TEST456";
        String secret  = "test_secret_key";

        String sig1 = hmacSha256(payload, secret);
        String sig2 = hmacSha256(payload, secret);

        assertNotNull(sig1);
        assertFalse(sig1.isEmpty());
        // Same input must always produce same signature
        assertEquals(sig1, sig2);
    }

    @Test
    void testHmacSignatureDiffersForDifferentPayloads() throws Exception {
        String secret = "test_secret_key";
        String sig1   = hmacSha256("order_A|pay_X", secret);
        String sig2   = hmacSha256("order_B|pay_Y", secret);

        assertNotEquals(sig1, sig2);
    }

    @Test
    void testHmacSignatureDiffersForDifferentSecrets() throws Exception {
        String payload = "order_TEST123|pay_TEST456";
        String sig1    = hmacSha256(payload, "secret_one");
        String sig2    = hmacSha256(payload, "secret_two");

        assertNotEquals(sig1, sig2);
    }

    @Test
    void testHmacOutputIsHexString() throws Exception {
        String sig = hmacSha256("data", "secret");
        // SHA256 produces 32 bytes = 64 hex chars
        assertEquals(64, sig.length());
        assertTrue(sig.matches("[0-9a-f]+"));
    }

    @Test
    void testAmountConversionToPaise() {
        // Razorpay requires amounts in paise (1 INR = 100 paise)
        double fareInRupees = 599.00;
        int    amountInPaise = (int) Math.round(fareInRupees * 100);
        assertEquals(59900, amountInPaise);
    }

    @Test
    void testAmountConversionRoundsCorrectly() {
        // Fare with decimals should round correctly
        double fareInRupees = 599.99;
        int    amountInPaise = (int) Math.round(fareInRupees * 100);
        assertEquals(59999, amountInPaise);
    }
}
