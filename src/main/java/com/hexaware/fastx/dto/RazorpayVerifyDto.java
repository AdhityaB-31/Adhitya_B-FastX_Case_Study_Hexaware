package com.hexaware.fastx.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Payload the frontend sends back after the Razorpay popup succeeds. */
@Getter
@Setter
@NoArgsConstructor
public class RazorpayVerifyDto {
    private String razorpayOrderId;    // order_XXXX
    private String razorpayPaymentId;  // pay_XXXX
    private String razorpaySignature;  // HMAC-SHA256 to verify
    private Long   bookingId;
    private String paymentMethod;      // UPI / CREDIT_CARD / DEBIT_CARD / NET_BANKING
}
