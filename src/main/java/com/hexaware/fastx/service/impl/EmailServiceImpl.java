package com.hexaware.fastx.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hexaware.fastx.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@fastx.com}")
    private String fromAddress;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Override
    public void sendPasswordResetEmail(String toEmail, String recipientName, String resetLink) {
        if (!mailEnabled) {
            // Lets the app run in environments without SMTP configured (e.g. local dev)
            // while still logging the link so the flow can be tested end-to-end.
            logger.warn("Mail sending disabled. Password reset link for {}: {}", toEmail, resetLink);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Reset your FastX password");

            String safeName = (recipientName == null || recipientName.isBlank()) ? "there" : recipientName;

            String html = "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:auto;\">"
                    + "<h2 style=\"color:#2563eb;\">FastX</h2>"
                    + "<p>Hi " + safeName + ",</p>"
                    + "<p>We received a request to reset your FastX account password. Click the button below to choose a new password. "
                    + "This link will expire in 30 minutes.</p>"
                    + "<p style=\"text-align:center;margin:32px 0;\">"
                    + "<a href=\"" + resetLink
                    + "\" style=\"background:#2563eb;color:#ffffff;padding:12px 24px;border-radius:6px;text-decoration:none;font-weight:bold;\">Reset Password</a>"
                    + "</p>"
                    + "<p>If the button doesn't work, copy and paste this link into your browser:</p>"
                    + "<p style=\"word-break:break-all;color:#2563eb;\">" + resetLink + "</p>"
                    + "<p>If you didn't request this, you can safely ignore this email — your password will remain unchanged.</p>"
                    + "<p style=\"color:#9ca3af;font-size:12px;margin-top:32px;\">FastX — Online Bus Ticket Booking System</p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(message);
            logger.info("Password reset email sent to {}", toEmail);
        } catch (MessagingException | MailException ex) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, ex.getMessage());
            // Do not leak SMTP errors to the caller / end user — the forgot-password
            // endpoint always responds generically regardless of email delivery outcome.
        }
    }

    @Override
    public void sendApprovalEmail(String toEmail, String recipientName) {
        if (!mailEnabled) {
            logger.warn("Mail sending disabled. Operator approval notification for {}: approved", toEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("FastX - Operator Account Approved!");

            String safeName = (recipientName == null || recipientName.isBlank()) ? "Operator" : recipientName;

            String html = "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:auto;\">"
                    + "<h2 style=\"color:#2563eb;\">FastX</h2>"
                    + "<p>Hi " + safeName + ",</p>"
                    + "<p>We are pleased to inform you that your FastX Bus Operator registration has been approved by the Administrator.</p>"
                    + "<p>You can now log in to your dashboard, manage your buses, and set up your journey routes.</p>"
                    + "<p style=\"color:#9ca3af;font-size:12px;margin-top:32px;\">FastX — Online Bus Ticket Booking System</p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(message);
            logger.info("Approval email sent to {}", toEmail);
        } catch (MessagingException | MailException ex) {
            logger.error("Failed to send approval email to {}: {}", toEmail, ex.getMessage());
        }
    }

    @Override
    public void sendDeactivationEmail(String toEmail, String recipientName, String reason) {
        if (!mailEnabled) {
            logger.warn("Mail sending disabled. Account deactivation notification for {} with reason: {}", toEmail,
                    reason);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("FastX - Account Deactivated");

            String safeName = (recipientName == null || recipientName.isBlank()) ? "User" : recipientName;

            String html = "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:auto;\">"
                    + "<h2 style=\"color:#ef4444;\">FastX - Account Deactivated</h2>"
                    + "<p>Hi " + safeName + ",</p>"
                    + "<p>We regret to inform you that your FastX account has been deactivated by the Administrator.</p>"
                    + "<p><strong>Reason for deactivation:</strong> " + reason + "</p>"
                    + "<p>If you believe this is a mistake or wish to reactivate your account, please contact our administrator.</p>"
                    + "<p style=\"color:#9ca3af;font-size:12px;margin-top:32px;\">FastX — Online Bus Ticket Booking System</p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(message);
            logger.info("Deactivation email sent to {}", toEmail);
        } catch (MessagingException | MailException ex) {
            logger.error("Failed to send deactivation email to {}: {}", toEmail, ex.getMessage());
        }
    }

    @Override
    public void sendActivationEmail(String toEmail, String recipientName) {
        if (!mailEnabled) {
            logger.warn("Mail sending disabled. Account activation notification for {}", toEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("FastX - Account Activated");

            String safeName = (recipientName == null || recipientName.isBlank()) ? "User" : recipientName;

            String html = "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:auto;\">"
                    + "<h2 style=\"color:#10b981;\">FastX - Account Activated</h2>"
                    + "<p>Hi " + safeName + ",</p>"
                    + "<p>We are pleased to inform you that your FastX account has been successfully activated.</p>"
                    + "<p>You can now log in to the portal and access all services.</p>"
                    + "<p style=\"color:#9ca3af;font-size:12px;margin-top:32px;\">FastX — Online Bus Ticket Booking System</p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(message);
            logger.info("Activation email sent to {}", toEmail);
        } catch (MessagingException | MailException ex) {
            logger.error("Failed to send activation email to {}: {}", toEmail, ex.getMessage());
        }
    }

    @Override
    public void sendRefundStatusEmail(String toEmail, String recipientName, String bookingId, String refundAmount,
            String status) {
        if (!mailEnabled) {
            logger.warn(
                    "Mail sending disabled. Refund status notification for {} (Booking ID: {}, Amount: {}, Status: {})",
                    toEmail, bookingId, refundAmount, status);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);

            boolean isApproved = "APPROVED".equalsIgnoreCase(status);
            String subject = "FastX - Refund " + (isApproved ? "Approved" : "Rejected");
            helper.setSubject(subject);

            String safeName = (recipientName == null || recipientName.isBlank()) ? "Customer" : recipientName;

            String color = isApproved ? "#10b981" : "#ef4444";
            String statusText = isApproved ? "APPROVED" : "REJECTED";
            String bodyMessage = isApproved
                    ? "We are pleased to inform you that your refund request has been <strong>approved</strong> by the bus operator. The refund amount will be credited back to your original payment method."
                    : "We regret to inform you that your refund request has been <strong>rejected</strong> by the bus operator. If you have any queries or feel this was in error, please contact support or your bus operator directly.";

            String html = "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:auto;\">"
                    + "<h2 style=\"color:#2563eb;\">FastX</h2>"
                    + "<p>Hi " + safeName + ",</p>"
                    + "<p>" + bodyMessage + "</p>"
                    + "<table style=\"width:100%;border-collapse:collapse;margin:20px 0;\">"
                    + "<tr><td style=\"padding:8px 0;font-weight:bold;color:#4b5563;\">Booking ID:</td><td style=\"padding:8px 0;\">"
                    + bookingId + "</td></tr>"
                    + "<tr><td style=\"padding:8px 0;font-weight:bold;color:#4b5563;\">Refund Amount:</td><td style=\"padding:8px 0;\">₹"
                    + refundAmount + "</td></tr>"
                    + "<tr><td style=\"padding:8px 0;font-weight:bold;color:#4b5563;\">Status:</td><td style=\"padding:8px 0;font-weight:bold;color:"
                    + color + ";\">" + statusText + "</td></tr>"
                    + "</table>"
                    + "<p style=\"color:#9ca3af;font-size:12px;margin-top:32px;\">FastX — Online Bus Ticket Booking System</p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(message);
            logger.info("Refund status email sent to {} for Booking {}", toEmail, bookingId);
        } catch (MessagingException | MailException ex) {
            logger.error("Failed to send refund status email to {}: {}", toEmail, ex.getMessage());
        }
    }
}
