package com.hexaware.fastx.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.BookingDto;
import com.hexaware.fastx.dto.BusDto;
import com.hexaware.fastx.dto.BusOperatorDto;
import com.hexaware.fastx.dto.PassengerDto;
import com.hexaware.fastx.dto.PaymentDto;
import com.hexaware.fastx.dto.RoleDto;
import com.hexaware.fastx.dto.SeatDto;
import com.hexaware.fastx.dto.UserDto;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private BusService busService;

    @Autowired
    private BusOperatorService busOperatorService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private SeatService seatService;

    // Create sample booking for payment testing
    private Long createTestBooking() {

        // Create Role
        RoleDto role = new RoleDto();
        role.setRoleName("ROLE_USER");
        RoleDto savedRole = roleService.createRole(role);

        // Create User
        UserDto user = new UserDto();
        user.setFullName("Adhitya");
        user.setEmail("adhitya" + System.nanoTime() + "@fastx.com");
        user.setPassword("password123");
        user.setPhoneNumber("9876543210");
        user.setGender("MALE");
        user.setAddress("Puducherry");
        user.setIsActive(true);
        user.setRoleId(savedRole.getRoleId());

        UserDto savedUser = userService.createUser(user);

        // Create Operator
        BusOperatorDto operator = new BusOperatorDto();
        operator.setCompanyName("FastX Travels");
        operator.setEmail("operator" + System.nanoTime() + "@fastx.com");
        operator.setPassword("password123");
        operator.setPhoneNumber("9876543211");
        operator.setAddress("Puducherry");

        BusOperatorDto savedOperator = busOperatorService.createOperator(operator);

        // Create Bus
        BusDto bus = new BusDto();
        bus.setBusName("FastX Express");
        bus.setBusNumber("TN01FX" + System.nanoTime() % 10000);
        bus.setBusType("AC_SLEEPER");
        bus.setOrigin("Puducherry");
        bus.setDestination("Chennai");
        bus.setJourneyDate(LocalDate.of(2026, 12, 20));
        bus.setDepartureTime(LocalTime.of(21, 0));
        bus.setArrivalTime(LocalTime.of(5, 0));
        bus.setFare(500.0);
        bus.setTotalSeats(40);
        bus.setOperatorId(savedOperator.getOperatorId());

        BusDto savedBus = busService.addBus(bus);

        // Get Available Seat
        List<SeatDto> availableSeats = seatService.getAvailableSeats(savedBus.getBusId());

        // Create Passenger
        PassengerDto passenger = new PassengerDto();
        passenger.setName("Buddy");
        passenger.setAge(21);
        passenger.setGender("MALE");
        passenger.setSeatId(
                availableSeats.get(0).getSeatId());

        List<PassengerDto> passengers = new ArrayList<>();
        passengers.add(passenger);

        // Create Booking
        BookingDto booking = new BookingDto();
        booking.setUserId(savedUser.getUserId());
        booking.setBusId(savedBus.getBusId());
        booking.setPassengers(passengers);

        BookingDto savedBooking = bookingService.createBooking(booking);

        return savedBooking.getBookingId();
    }

    @Test
    void testGetPaymentByBookingId() {

        Long bookingId = createTestBooking();

        PaymentDto payment = paymentService.getPaymentByBookingId(
                bookingId);

        assertNotNull(payment);

        assertEquals(
                "PENDING",
                payment.getPaymentStatus());

        assertEquals(
                500.0,
                payment.getAmount());
    }

    @Test
    void testProcessPayment() {

        Long bookingId = createTestBooking();

        PaymentDto payment = paymentService.processPayment(
                bookingId,
                "UPI");

        assertNotNull(payment);
        assertNotNull(payment.getPaymentId());
        assertNotNull(payment.getTransactionId());

        assertEquals(
                "COMPLETED",
                payment.getPaymentStatus());
    }

    @Test
    void testVerifyPayment() {

        Long bookingId = createTestBooking();

        PaymentDto payment = paymentService.processPayment(
                bookingId,
                "CREDIT_CARD");

        PaymentDto result = paymentService.verifyPayment(
                payment.getPaymentId());

        assertNotNull(result);

        assertEquals(
                "COMPLETED",
                result.getPaymentStatus());
    }

    @Test
    void testRefundPayment() {

        Long bookingId = createTestBooking();

        PaymentDto payment = paymentService.processPayment(
                bookingId,
                "DEBIT_CARD");

        PaymentDto result = paymentService.refundPayment(
                payment.getPaymentId());

        assertNotNull(result);

        assertEquals(
                "REFUNDED",
                result.getPaymentStatus());
    }

    @Test
    void testProcessPaymentWithTransaction() {
        Long bookingId = createTestBooking();

        PaymentDto payment = paymentService.processPaymentWithTransaction(
                bookingId, "UPI", "pay_TEST_RAZORPAY123");

        assertNotNull(payment);
        assertEquals("COMPLETED", payment.getPaymentStatus());
        assertEquals("pay_TEST_RAZORPAY123", payment.getTransactionId());
    }

}