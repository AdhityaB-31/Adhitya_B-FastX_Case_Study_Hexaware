package com.hexaware.fastx.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.BookingDto;
import com.hexaware.fastx.dto.PassengerDto;
import com.hexaware.fastx.entity.Booking;
import com.hexaware.fastx.entity.Bus;
import com.hexaware.fastx.entity.Passenger;
import com.hexaware.fastx.entity.Payment;
import com.hexaware.fastx.entity.Seat;
import com.hexaware.fastx.entity.User;
import com.hexaware.fastx.enums.BookingStatus;
import com.hexaware.fastx.enums.Gender;
import com.hexaware.fastx.enums.PaymentStatus;
import com.hexaware.fastx.entity.Refund;
import com.hexaware.fastx.enums.RefundStatus;
import com.hexaware.fastx.enums.SeatStatus;
import com.hexaware.fastx.exception.BookingException;
import com.hexaware.fastx.exception.BusNotFoundException;
import com.hexaware.fastx.exception.ResourceNotFoundException;
import com.hexaware.fastx.exception.SeatNotAvailableException;
import com.hexaware.fastx.exception.UnauthorizedException;
import com.hexaware.fastx.repository.BookingRepository;
import com.hexaware.fastx.repository.RefundRepository;
import com.hexaware.fastx.repository.BusRepository;
import com.hexaware.fastx.repository.PassengerRepository;
import com.hexaware.fastx.repository.PaymentRepository;
import com.hexaware.fastx.repository.SeatRepository;
import com.hexaware.fastx.repository.UserRepository;
import com.hexaware.fastx.service.BookingService;
import com.hexaware.fastx.service.SeatService;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private static final int RESERVATION_TIMEOUT_MINUTES = 30;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private SeatService seatService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Override
    @Transactional
    public BookingDto createBooking(BookingDto bookingDto) {
        logger.info("Creating booking for User ID: {} on Bus ID: {}", bookingDto.getUserId(), bookingDto.getBusId());

        User user = userRepository.findById(bookingDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + bookingDto.getUserId()));

        Bus bus = busRepository.findById(bookingDto.getBusId())
                .orElseThrow(() -> new BusNotFoundException("Bus not found with ID: " + bookingDto.getBusId()));

        int numberOfPassengers = bookingDto.getPassengers().size();
        logger.info("Number of passengers: {}", numberOfPassengers);

        for (PassengerDto pDto : bookingDto.getPassengers()) {
            Seat seat = seatRepository.findById(pDto.getSeatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + pDto.getSeatId()));

            if (!seat.getBus().getBusId().equals(bookingDto.getBusId())) {
                throw new BookingException(
                        "Seat " + seat.getSeatNumber() + " does not belong to Bus ID: " + bookingDto.getBusId());
            }

            if (seat.getSeatStatus() == SeatStatus.RESERVED) {
                throw new SeatNotAvailableException("Seat " + seat.getSeatNumber()
                        + " is currently reserved by another user. Please choose a different seat.");
            }
            if (seat.getSeatStatus() == SeatStatus.BOOKED) {
                throw new SeatNotAvailableException("Seat " + seat.getSeatNumber() + " is already booked");
            }
        }

        Long availableCount = seatRepository.countSeatsByBusIdAndStatus(bus.getBusId(), SeatStatus.AVAILABLE);
        if (availableCount < numberOfPassengers) {
            throw new SeatNotAvailableException(
                    "Not enough seats available. Requested: " + numberOfPassengers + ", Available: " + availableCount);
        }

        Double totalAmount = bus.getFare() * numberOfPassengers;
        logger.info("Total amount calculated: {} (fare {} x {} passengers)", totalAmount, bus.getFare(),
                numberOfPassengers);

        Booking booking = new Booking();
        booking.setBookingDate(LocalDate.now());
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setTotalAmount(totalAmount);
        booking.setUser(user);
        booking.setBus(bus);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RESERVATION_TIMEOUT_MINUTES);
        booking.setReservationExpiresAt(expiresAt);
        logger.info("Reservation expires at: {} (30-minute window)", expiresAt);

        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Booking saved with auto-generated ID: {}. User will use this ID to complete payment.",
                savedBooking.getBookingId());

        List<Long> seatIds = new ArrayList<>();
        for (PassengerDto pDto : bookingDto.getPassengers()) {
            seatIds.add(pDto.getSeatId());
        }

        seatService.reserveSeats(seatIds, savedBooking.getBookingId());

        List<Passenger> passengers = new ArrayList<>();
        for (PassengerDto pDto : bookingDto.getPassengers()) {
            Passenger passenger = new Passenger();
            passenger.setName(pDto.getName());
            passenger.setAge(pDto.getAge());
            if (pDto.getGender() != null) {
                passenger.setGender(Gender.valueOf(pDto.getGender()));
            }
            Seat seat = seatRepository.findById(pDto.getSeatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + pDto.getSeatId()));
            passenger.setSeat(seat);
            passenger.setBooking(savedBooking);

            Passenger savedPassenger = passengerRepository.save(passenger);
            passengers.add(savedPassenger);
            logger.info("Passenger added: {} (Age: {}) -> Seat: {}", pDto.getName(), pDto.getAge(),
                    seat.getSeatNumber());
        }
        savedBooking.setPassengers(passengers);

        Payment payment = new Payment();
        payment.setAmount(totalAmount);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setBooking(savedBooking);
        paymentRepository.save(payment);
        savedBooking.setPayment(payment);
        logger.info("Pending payment record created for Booking ID: {}", savedBooking.getBookingId());

        logger.info(
                "Booking created successfully! Booking ID: {} | Status: PENDING | Seats: RESERVED for 30 mins | Complete payment to confirm.",
                savedBooking.getBookingId());
        return mapToDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto confirmBooking(Long bookingId) {
        logger.info("Confirming booking with ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            logger.warn("Cannot confirm booking ID: {}. Current status: {}", bookingId, booking.getBookingStatus());
            throw new BookingException("Booking cannot be confirmed. Current status: " + booking.getBookingStatus());
        }

        if (booking.getReservationExpiresAt() != null
                && LocalDateTime.now().isAfter(booking.getReservationExpiresAt())) {
            logger.warn("Reservation expired for Booking ID: {}. Expiry was: {}", bookingId,
                    booking.getReservationExpiresAt());
            expireBooking(booking);
            throw new BookingException(
                    "Reservation has expired. The 30-minute payment window has passed. Seats have been released. Please create a new booking.");
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);

        if (booking.getPassengers() != null && !booking.getPassengers().isEmpty()) {
            List<Long> seatIds = new ArrayList<>();
            for (Passenger p : booking.getPassengers()) {
                seatIds.add(p.getSeat().getSeatId());
            }
            seatService.confirmSeats(seatIds);
            logger.info("Seats confirmed (RESERVED -> BOOKED) for Booking ID: {}", bookingId);
        }

        Booking confirmed = bookingRepository.save(booking);
        logger.info("Booking confirmed successfully. Booking ID: {} | Seats are now permanently BOOKED.",
                confirmed.getBookingId());
        return mapToDto(confirmed);
    }

    @Override
    @Transactional
    public BookingDto cancelBooking(Long bookingId) {
        return cancelBooking(bookingId, null);
    }

    @Override
    @Transactional
    public BookingDto cancelBooking(Long bookingId, String reason) {
        logger.info("Cancelling booking with ID: {} and reason: {}", bookingId, reason);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            logger.warn("Booking ID: {} is already cancelled", bookingId);
            throw new BookingException("Booking is already cancelled");
        }
        if (booking.getBookingStatus() == BookingStatus.EXPIRED) {
            logger.warn("Booking ID: {} is already expired", bookingId);
            throw new BookingException("Booking has already expired. Seats were auto-released.");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);

        if (booking.getPassengers() != null && !booking.getPassengers().isEmpty()) {
            List<Long> seatIds = new ArrayList<>();
            for (Passenger p : booking.getPassengers()) {
                seatIds.add(p.getSeat().getSeatId());
            }
            seatService.releaseSeats(seatIds);
            logger.info("Released {} seats for cancelled Booking ID: {}", seatIds.size(), bookingId);

            passengerRepository.deleteAll(booking.getPassengers());
            passengerRepository.flush();
            booking.getPassengers().clear();
            logger.info("Deleted {} passenger records for cancelled Booking ID: {}", seatIds.size(), bookingId);
        }

        Booking cancelled = bookingRepository.save(booking);

        boolean paymentWasCompleted = false;
        if (booking.getPayment() != null
                && booking.getPayment().getPaymentStatus() == PaymentStatus.COMPLETED) {
            paymentWasCompleted = true;
        }

        if (paymentWasCompleted) {
            boolean refundAlreadyExists = refundRepository.findByBookingId(bookingId).isPresent();
            if (!refundAlreadyExists) {
                Refund refund = new Refund();
                refund.setRefundAmount(booking.getTotalAmount());
                refund.setRefundDate(java.time.LocalDateTime.now());
                refund.setRefundStatus(RefundStatus.PENDING);
                if (reason != null && !reason.trim().isEmpty()) {
                    refund.setReason(reason);
                } else {
                    refund.setReason("User cancelled confirmed booking " + bookingId);
                }
                refund.setBooking(cancelled);
                refund = refundRepository.save(refund);
                cancelled.setRefund(refund);
                logger.info("Refund auto-created for cancelled Booking ID: {}. Amount: {}", bookingId,
                        booking.getTotalAmount());

                Payment payment = booking.getPayment();
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
                logger.info("Payment marked as REFUNDED for Booking ID: {}", bookingId);
            } else {
                logger.info("Refund already exists for Booking ID: {} — skipping duplicate creation", bookingId);
                cancelled.setRefund(refundRepository.findByBookingId(bookingId).orElse(null));
            }
        } else {
            logger.info("No refund created for Booking ID: {} — payment was not completed (status: PENDING/no payment)",
                    bookingId);
        }

        logger.info("Booking cancelled successfully. Booking ID: {} | Refund created: {}", cancelled.getBookingId(),
                paymentWasCompleted);
        return mapToDto(cancelled);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long bookingId) {
        logger.info("Fetching booking with ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        return mapToDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByUser(Long userId) {
        logger.info("Fetching bookings for User ID: {}", userId);
        List<Booking> bookings = bookingRepository.findBookingsByUserId(userId);
        List<BookingDto> bookingDtos = new ArrayList<>();

        for (Booking booking : bookings) {
            bookingDtos.add(mapToDto(booking));
        }

        logger.info("Found {} bookings for User ID: {}", bookingDtos.size(), userId);
        return bookingDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByBus(Long busId) {
        logger.info("Fetching bookings for Bus ID: {}", busId);
        List<Booking> bookings = bookingRepository.findBookingsByBusId(busId);
        List<BookingDto> bookingDtos = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingDtos.add(mapToDto(booking));
        }
        logger.info("Found {} bookings for Bus ID: {}", bookingDtos.size(), busId);
        return bookingDtos;
    }

    @Override
    public Double calculateFare(Long busId, int numberOfSeats) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new BusNotFoundException("Bus not found with ID: " + busId));
        Double fare = bus.getFare() * numberOfSeats;
        logger.info("Fare calculated: {} for {} seats on Bus ID: {}", fare, numberOfSeats, busId);
        return fare;
    }

    @Override
    public void verifyBookingOwnership(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (booking.getUser() == null || !booking.getUser().getUserId().equals(userId)) {
            logger.warn("User ID {} attempted to access Booking ID {} which belongs to a different user", userId,
                    bookingId);
            throw new UnauthorizedException(
                    "You do not own Booking ID: " + bookingId + ". You can only manage your own bookings.");
        }
    }

    @Override
    public void verifyBusOperatorOwnership(Long busId, Long operatorId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new com.hexaware.fastx.exception.BusNotFoundException("Bus not found with ID: " + busId));
        if (bus.getBusOperator() == null || !bus.getBusOperator().getOperatorId().equals(operatorId)) {
            logger.warn("Operator ID {} attempted to access Bus ID {} which belongs to a different operator", operatorId, busId);
            throw new UnauthorizedException("You do not own this Bus ID: " + busId);
        }
    }

    /**
     * Expires a booking and releases its reserved seats.
     * Called when a user tries to confirm/pay after the 30-minute window has
     * passed.
     */
    @Transactional
    public void expireBooking(Booking booking) {
        logger.info("Expiring Booking ID: {} - reservation window has passed", booking.getBookingId());
        booking.setBookingStatus(BookingStatus.EXPIRED);

        if (booking.getPassengers() != null && !booking.getPassengers().isEmpty()) {
            List<Long> seatIds = new ArrayList<>();
            for (Passenger p : booking.getPassengers()) {
                seatIds.add(p.getSeat().getSeatId());
            }
            seatService.releaseSeats(seatIds);
            logger.info("Released {} seats for expired Booking ID: {}", seatIds.size(), booking.getBookingId());

            // Delete stale passenger records so the seat can be rebooked without
            // hitting the unique constraint on seat_id
            passengerRepository.deleteAll(booking.getPassengers());
            passengerRepository.flush();
            booking.getPassengers().clear();
            logger.info("Deleted {} passenger records for expired Booking ID: {}", seatIds.size(),
                    booking.getBookingId());
        }

        bookingRepository.save(booking);
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 30000)
    @Transactional
    public void autoCancelExpiredBookings() {
        List<Booking> expiredBookings = bookingRepository.findByBookingStatusAndReservationExpiresAtBefore(
                BookingStatus.PENDING, LocalDateTime.now());
        if (!expiredBookings.isEmpty()) {
            logger.info("Found {} expired PENDING bookings. Auto-cancelling...", expiredBookings.size());
            for (Booking booking : expiredBookings) {
                expireBooking(booking);
            }
        }
    }

    private BookingDto mapToDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setBookingId(booking.getBookingId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setBookingStatus(booking.getBookingStatus() != null ? booking.getBookingStatus().name() : null);
        dto.setReservationExpiresAt(booking.getReservationExpiresAt());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setUserId(booking.getUser() != null ? booking.getUser().getUserId() : null);
        dto.setBusId(booking.getBus() != null ? booking.getBus().getBusId() : null);
        dto.setNumberOfSeats(booking.getPassengers() != null ? booking.getPassengers().size() : 0);

        if (booking.getBus() != null) {
            com.hexaware.fastx.entity.Bus b = booking.getBus();
            dto.setBusName(b.getBusName());
            dto.setBusNumber(b.getBusNumber());
            dto.setBusType(b.getBusType() != null ? b.getBusType().name() : null);
            dto.setOrigin(b.getOrigin());
            dto.setDestination(b.getDestination());
            dto.setJourneyDate(b.getJourneyDate());
            dto.setDepartureTime(b.getDepartureTime());
            dto.setArrivalTime(b.getArrivalTime());
            dto.setFarePerSeat(b.getFare());
        }

        if (booking.getRefund() != null) {
            dto.setRefundId(booking.getRefund().getRefundId());
            dto.setRefundStatus(
                    booking.getRefund().getRefundStatus() != null ? booking.getRefund().getRefundStatus().name()
                            : null);
            dto.setRefundAmount(booking.getRefund().getRefundAmount());
        }

        if (booking.getPassengers() != null) {
            List<PassengerDto> passengerDtos = new ArrayList<>();
            for (Passenger p : booking.getPassengers()) {
                PassengerDto pDto = new PassengerDto();
                pDto.setPassengerId(p.getPassengerId());
                pDto.setName(p.getName());
                pDto.setAge(p.getAge());
                pDto.setGender(p.getGender() != null ? p.getGender().name() : null);
                pDto.setSeatId(p.getSeat() != null ? p.getSeat().getSeatId() : null);
                pDto.setSeatNumber(p.getSeat() != null ? p.getSeat().getSeatNumber() : null);
                pDto.setSeatType(
                        p.getSeat() != null && p.getSeat().getSeatType() != null ? p.getSeat().getSeatType().name()
                                : null);
                pDto.setBookingId(booking.getBookingId());
                passengerDtos.add(pDto);
            }
            dto.setPassengers(passengerDtos);
        }

        return dto;
    }
}
