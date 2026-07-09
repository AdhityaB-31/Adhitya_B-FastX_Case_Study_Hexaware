package com.hexaware.fastx.controller;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hexaware.fastx.dto.BookingDto;
import com.hexaware.fastx.dto.PaymentDto;
import com.hexaware.fastx.dto.RazorpayOrderDto;
import com.hexaware.fastx.dto.RazorpayVerifyDto;
import com.hexaware.fastx.service.BookingService;
import com.hexaware.fastx.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@RestController
@RequestMapping("/api/razorpay")
public class RazorpayController {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayController.class);

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        try {
            Long bookingId = Long.valueOf(body.get("bookingId").toString());
            BookingDto booking = bookingService.getBookingById(bookingId);

            if (booking.getTotalAmount() == null || booking.getTotalAmount() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking has no payable amount"));
            }

            int amountInPaise = (int) Math.round(booking.getTotalAmount() * 100);

            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put("receipt", "fastx_bk_" + bookingId);
            options.put("notes", new JSONObject()
                    .put("bookingId", bookingId)
                    .put("app", "FastX Bus Booking"));

            Order order = razorpayClient.orders.create(options);
            logger.info("Razorpay order created: {} for booking #{} amount ₹{}",
                    order.get("id"), bookingId, booking.getTotalAmount());

            RazorpayOrderDto response = new RazorpayOrderDto(
                    order.get("id").toString(),
                    bookingId,
                    amountInPaise,
                    "INR",
                    keyId // send public key to frontend — safe
            );
            return ResponseEntity.ok(response);

        } catch (RazorpayException e) {
            logger.error("Razorpay order creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Payment gateway error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating Razorpay order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody RazorpayVerifyDto dto) {
        try {
            String payload = dto.getRazorpayOrderId() + "|" + dto.getRazorpayPaymentId();
            String generatedSignature = hmacSha256(payload, keySecret);

            if (!generatedSignature.equals(dto.getRazorpaySignature())) {
                logger.warn("Razorpay signature mismatch for booking #{}", dto.getBookingId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Payment verification failed — invalid signature"));
            }

            String method = dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "UPI";
            PaymentDto payment = paymentService.processPaymentWithTransaction(
                    dto.getBookingId(), method, dto.getRazorpayPaymentId());

            logger.info("Payment verified & confirmed. Booking #{} | Razorpay payment: {}",
                    dto.getBookingId(), dto.getRazorpayPaymentId());

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "bookingId", dto.getBookingId(),
                    "paymentId", payment.getPaymentId(),
                    "transactionId", dto.getRazorpayPaymentId(),
                    "amount", payment.getAmount()));

        } catch (Exception e) {
            logger.error("Payment verification error for booking #{}: {}", dto.getBookingId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment verification error: " + e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        try {
            if (webhookSecret != null && !webhookSecret.isEmpty()) {
                String expectedSig = hmacSha256(payload, webhookSecret);
                if (!expectedSig.equals(signature)) {
                    logger.warn("Webhook signature mismatch — possible spoofed request");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
                }
            }

            JSONObject event = new JSONObject(payload);
            String eventType = event.getString("event");
            logger.info("Razorpay webhook received: {}", eventType);

            if ("payment.captured".equals(eventType)) {
                JSONObject paymentEntity = event
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String razorpayPaymentId = paymentEntity.getString("id");
                JSONObject notes = paymentEntity.optJSONObject("notes");

                if (notes != null && notes.has("bookingId")) {
                    Long bookingId = notes.getLong("bookingId");
                    try {
                        bookingService.confirmBooking(bookingId);
                        logger.info("Webhook: booking #{} confirmed via payment.captured ({})",
                                bookingId, razorpayPaymentId);
                    } catch (Exception ex) {
                        logger.info("Webhook: booking #{} already confirmed — skipping", bookingId);
                    }
                }
            }

            if ("payment.failed".equals(eventType)) {
                JSONObject paymentEntity = event
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");
                logger.warn("Razorpay payment.failed — payment ID: {}, description: {}",
                        paymentEntity.optString("id"),
                        paymentEntity.optJSONObject("error_description"));
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            logger.error("Webhook processing error: {}", e.getMessage());
            return ResponseEntity.ok("OK");
        }
    }

    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
