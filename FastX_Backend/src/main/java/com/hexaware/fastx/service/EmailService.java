package com.hexaware.fastx.service;

public interface EmailService {

    /**
     * Sends a password-reset email containing a link back to the frontend's
     * reset-password page, with the raw (unhashed) token as a query parameter.
     */
    void sendPasswordResetEmail(String toEmail, String recipientName, String resetLink);
    void sendApprovalEmail(String toEmail, String recipientName);
    void sendDeactivationEmail(String toEmail, String recipientName, String reason);
    void sendActivationEmail(String toEmail, String recipientName);
    void sendRefundStatusEmail(String toEmail, String recipientName, String bookingId, String refundAmount, String status);
}

