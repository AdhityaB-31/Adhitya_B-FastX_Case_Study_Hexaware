package com.hexaware.fastx.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Sent to the frontend after creating a Razorpay order. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderDto {
    private String razorpayOrderId;   // order_XXXX — from Razorpay
    private Long   bookingId;
    private Integer amountInPaise;    // e.g. 59900 for ₹599
    private String currency;          // "INR"
    private String keyId;             // public key — safe to expose to frontend
}
