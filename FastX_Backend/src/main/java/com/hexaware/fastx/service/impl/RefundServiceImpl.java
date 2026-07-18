package com.hexaware.fastx.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.RefundDto;
import com.hexaware.fastx.entity.Booking;
import com.hexaware.fastx.entity.Payment;
import com.hexaware.fastx.entity.Refund;
import com.hexaware.fastx.enums.BookingStatus;
import com.hexaware.fastx.enums.RefundStatus;
import com.hexaware.fastx.exception.BookingException;
import com.hexaware.fastx.exception.ResourceNotFoundException;
import com.hexaware.fastx.exception.UnauthorizedException;
import com.hexaware.fastx.repository.BookingRepository;
import com.hexaware.fastx.repository.PaymentRepository;
import com.hexaware.fastx.repository.RefundRepository;
import com.hexaware.fastx.service.RefundService;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
@Transactional
public class RefundServiceImpl implements RefundService {

    private static final Logger logger = LoggerFactory.getLogger(RefundServiceImpl.class);

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private com.hexaware.fastx.service.EmailService emailService;

    @Override
    public RefundDto createRefund(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (booking.getBookingStatus() != BookingStatus.CANCELLED) {
            throw new BookingException(
                    "Refund can only be created for cancelled bookings. Current status: " + booking.getBookingStatus());
        }

        Optional<Refund> existing = refundRepository.findByBookingId(bookingId);
        if (existing.isPresent()) {
            logger.info("Refund already exists for Booking ID: {} — returning existing refund", bookingId);
            return mapToDto(existing.get());
        }

        Refund refund = new Refund();

        refund.setRefundAmount(booking.getTotalAmount());
        refund.setRefundDate(LocalDateTime.now());
        refund.setRefundStatus(RefundStatus.PENDING);
        refund.setReason("User requested refund for booking " + bookingId);
        refund.setBooking(booking);

        Refund saved = refundRepository.save(refund);
        logger.info("Refund created for Booking ID: {}. Refund Amount: {}", bookingId, saved.getRefundAmount());
        return mapToDto(saved);
    }

    @Override
    public RefundDto approveRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found with ID: " + refundId));

        if (refund.getRefundStatus() == RefundStatus.APPROVED) {
            throw new BookingException("Refund #" + refundId + " has already been approved.");
        }

        Long bookingId = refund.getBooking().getBookingId();
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No payment record found for Booking ID: " + bookingId +
                                ". Cannot process Razorpay refund."));

        String razorpayPaymentId = payment.getTransactionId();

        if (razorpayPaymentId == null || razorpayPaymentId.isBlank() || !razorpayPaymentId.startsWith("pay_")) {
            logger.warn("Booking #{} has no Razorpay payment ID (transactionId={}). " +
                    "Marking refund APPROVED in DB only (no gateway refund).",
                    bookingId, razorpayPaymentId);
        } else {
            try {
                int amountInPaise = (int) Math.round(refund.getRefundAmount() * 100);

                JSONObject refundOptions = new JSONObject();
                refundOptions.put("amount", amountInPaise);
                refundOptions.put("notes", new JSONObject()
                        .put("bookingId", bookingId)
                        .put("refundId", refundId)
                        .put("reason", refund.getReason() != null ? refund.getReason() : "Admin approved refund"));

                com.razorpay.Refund rzpRefund = razorpayClient.payments.refund(razorpayPaymentId, refundOptions);
                String rzpRefundId = rzpRefund.get("id").toString();

                logger.info("Razorpay refund initiated. Booking #{} | Razorpay refund ID: {} | Amount: ₹{}",
                        bookingId, rzpRefundId, refund.getRefundAmount());

            } catch (RazorpayException e) {
                logger.error("Razorpay refund FAILED for Booking #{}: {}", bookingId, e.getMessage());
                logger.warn("Marking refund APPROVED in DB only because gateway failed. Manual refund may be needed.");
                
                String existingReason = refund.getReason() != null ? refund.getReason() : "";
                refund.setReason(existingReason + " [Gateway Refund Failed. Action Required on Dashboard]");
            }
        }

        refund.setRefundStatus(RefundStatus.APPROVED);
        Refund approved = refundRepository.save(refund);
        logger.info("Refund #{} approved in DB for Booking #{}.", refundId, bookingId);

        try {
            if (approved.getBooking() != null && approved.getBooking().getUser() != null) {
                com.hexaware.fastx.entity.User user = approved.getBooking().getUser();
                emailService.sendRefundStatusEmail(
                    user.getEmail(),
                    user.getFullName(),
                    approved.getBooking().getBookingId().toString(),
                    approved.getRefundAmount().toString(),
                    "APPROVED"
                );
            }
        } catch (Exception e) {
            logger.error("Failed to send refund approval email for refund #{}: {}", refundId, e.getMessage());
        }

        return mapToDto(approved);
    }

    @Override
    public RefundDto getRefundStatus(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found with ID: " + refundId));
        return mapToDto(refund);
    }

    @Override
    public List<RefundDto> getRefundsByStatus(RefundStatus status) {

        List<Refund> refunds = refundRepository.findByRefundStatus(status);

        List<RefundDto> refundDtos = new ArrayList<>();

        for (Refund refund : refunds) {
            refundDtos.add(mapToDto(refund));
        }

        return refundDtos;
    }

    private RefundDto mapToDto(Refund refund) {
        RefundDto dto = new RefundDto();
        dto.setRefundId(refund.getRefundId());
        dto.setRefundAmount(refund.getRefundAmount());
        dto.setRefundDate(refund.getRefundDate());
        dto.setRefundStatus(refund.getRefundStatus() != null ? refund.getRefundStatus().name() : null);
        dto.setReason(refund.getReason());
        dto.setBookingId(refund.getBooking() != null ? refund.getBooking().getBookingId() : null);
        return dto;
    }

    @Override
    public List<RefundDto> getAllRefunds() {
        List<Refund> refunds = refundRepository.findAll();
        List<RefundDto> refundDtos = new ArrayList<>();
        for (Refund refund : refunds) {
            refundDtos.add(mapToDto(refund));
        }
        return refundDtos;
    }

    @Override
    public RefundDto getRefundByBookingId(Long bookingId) {
        Refund refund = refundRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No refund found for Booking ID: " + bookingId));
        return mapToDto(refund);
    }

    @Override
    public RefundDto rejectRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new com.hexaware.fastx.exception.ResourceNotFoundException(
                        "Refund not found: " + refundId));
        refund.setRefundStatus(com.hexaware.fastx.enums.RefundStatus.REJECTED);
        Refund rejected = refundRepository.save(refund);

        try {
            if (rejected.getBooking() != null && rejected.getBooking().getUser() != null) {
                com.hexaware.fastx.entity.User user = rejected.getBooking().getUser();
                emailService.sendRefundStatusEmail(
                    user.getEmail(),
                    user.getFullName(),
                    rejected.getBooking().getBookingId().toString(),
                    rejected.getRefundAmount().toString(),
                    "REJECTED"
                );
            }
        } catch (Exception e) {
            logger.error("Failed to send refund rejection email for refund #{}: {}", refundId, e.getMessage());
        }

        return mapToDto(rejected);
    }

    @Override
    public List<RefundDto> getRefundsByOperator(Long operatorId) {
        List<Refund> refunds = refundRepository.findByOperatorId(operatorId);
        List<RefundDto> refundDtos = new ArrayList<>();
        for (Refund refund : refunds) {
            refundDtos.add(mapToDto(refund));
        }
        return refundDtos;
    }

    @Override
    public void verifyOperatorRefundOwnership(Long refundId, Long operatorId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found with ID: " + refundId));
        if (refund.getBooking() == null || refund.getBooking().getBus() == null ||
            refund.getBooking().getBus().getBusOperator() == null ||
            !refund.getBooking().getBus().getBusOperator().getOperatorId().equals(operatorId)) {
            throw new UnauthorizedException("You do not have permission to manage refunds for this booking.");
        }
    }

}