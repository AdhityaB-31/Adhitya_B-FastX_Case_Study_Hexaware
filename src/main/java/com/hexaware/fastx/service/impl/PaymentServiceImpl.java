package com.hexaware.fastx.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.PaymentDto;
import com.hexaware.fastx.entity.Booking;
import com.hexaware.fastx.entity.Payment;
import com.hexaware.fastx.enums.BookingStatus;
import com.hexaware.fastx.enums.PaymentMethod;
import com.hexaware.fastx.enums.PaymentStatus;
import com.hexaware.fastx.exception.BookingException;
import com.hexaware.fastx.exception.PaymentException;
import com.hexaware.fastx.exception.ResourceNotFoundException;
import com.hexaware.fastx.repository.BookingRepository;
import com.hexaware.fastx.repository.PaymentRepository;
import com.hexaware.fastx.service.BookingService;
import com.hexaware.fastx.service.PaymentService;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    @Override
    public PaymentDto getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Payment record not found for Booking ID: " + bookingId));
        return mapToDto(payment);
    }

    @Override
    @Transactional
    public PaymentDto processPayment(Long bookingId, String paymentMethod) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new BookingException("Cannot process payment. Booking status is: " + booking.getBookingStatus()
                    + ". Only PENDING bookings can be paid for.");
        }

        if (booking.getReservationExpiresAt() != null
                && LocalDateTime.now().isAfter(booking.getReservationExpiresAt())) {
            logger.warn("Payment attempted after reservation expired for Booking ID: {}", bookingId);
            throw new BookingException(
                    "Reservation has expired. The 30-minute payment window has passed. Please create a new booking.");
        }

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(
                        () -> new PaymentException("Pending payment record not found for Booking ID: " + bookingId));

        payment.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        logger.info("Payment processed successfully for Booking ID: {}. Transaction ID: {}", bookingId,
                saved.getTransactionId());

        bookingService.confirmBooking(bookingId);
        logger.info("Booking auto-confirmed after payment. Booking ID: {} | Seats are now permanently BOOKED.",
                bookingId);

        return mapToDto(saved);
    }

    @Override
    public PaymentDto verifyPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        return mapToDto(payment);
    }

    @Override
    public PaymentDto refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentException("Payment already refunded");
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        Payment refunded = paymentRepository.save(payment);
        logger.info("Payment refunded for ID: {}", paymentId);
        return mapToDto(refunded);
    }


    @Override
    @Transactional
    public PaymentDto processPaymentWithTransaction(Long bookingId, String paymentMethod, String transactionId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new BookingException("Booking " + bookingId + " is not in PENDING state: " + booking.getBookingStatus());
        }

        if (booking.getReservationExpiresAt() != null
                && java.time.LocalDateTime.now().isAfter(booking.getReservationExpiresAt())) {
            throw new BookingException("Reservation has expired for booking " + bookingId);
        }

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentException("No pending payment found for booking: " + bookingId));

        payment.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
        payment.setTransactionId(transactionId);          // real Razorpay pay_XXXX
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(java.time.LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        logger.info("Razorpay payment recorded. Booking #{} | TxnID: {}", bookingId, transactionId);

        bookingService.confirmBooking(bookingId);
        logger.info("Booking #{} auto-confirmed after Razorpay payment.", bookingId);

        return mapToDto(saved);
    }

    private PaymentDto mapToDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(payment.getPaymentId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentStatus(payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : null);
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setBookingId(payment.getBooking() != null ? payment.getBooking().getBookingId() : null);
        return dto;
    }
}
