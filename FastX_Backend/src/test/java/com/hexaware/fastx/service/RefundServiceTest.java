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
import com.hexaware.fastx.dto.RefundDto;
import com.hexaware.fastx.dto.RoleDto;
import com.hexaware.fastx.dto.SeatDto;
import com.hexaware.fastx.dto.UserDto;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefundServiceTest {

    @Autowired
    private RefundService refundService;

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

    // Create a cancelled booking for refund testing
    private Long createCancelledBooking() {

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

        // Create Bus Operator
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
        bus.setJourneyDate(LocalDate.of(2026, 12, 25));
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

        // Cancel Booking
        bookingService.cancelBooking(
                savedBooking.getBookingId());

        return savedBooking.getBookingId();
    }

    @Test
    void testCreateRefund() {

        Long bookingId = createCancelledBooking();

        RefundDto refund = refundService.createRefund(bookingId);

        assertNotNull(refund);
        assertNotNull(refund.getRefundId());

        assertEquals(
                "PENDING",
                refund.getRefundStatus());
    }

    @Test
    void testApproveRefund() {

        Long bookingId = createCancelledBooking();

        RefundDto refund = refundService.createRefund(bookingId);

        RefundDto result = refundService.approveRefund(
                refund.getRefundId());

        assertNotNull(result);

        assertEquals(
                "APPROVED",
                result.getRefundStatus());
    }

    @Test
    void testGetRefundStatus() {

        Long bookingId = createCancelledBooking();

        RefundDto refund = refundService.createRefund(bookingId);

        RefundDto result = refundService.getRefundStatus(
                refund.getRefundId());

        assertNotNull(result);

        assertEquals(
                refund.getRefundId(),
                result.getRefundId());
    }
    @Test
    void testGetRefundsByStatus() {

        Long bookingId = createCancelledBooking();

        refundService.createRefund(bookingId);

        List<RefundDto> results = refundService.getRefundsByStatus(com.hexaware.fastx.enums.RefundStatus.PENDING);

        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    void testRejectRefund() {
        Long bookingId = createCancelledBooking();
        RefundDto refund = refundService.createRefund(bookingId);

        RefundDto result = refundService.rejectRefund(refund.getRefundId());

        assertNotNull(result);
        assertEquals("REJECTED", result.getRefundStatus());
    }

    @Test
    void testGetRefundByBookingId() {
        Long bookingId = createCancelledBooking();
        refundService.createRefund(bookingId);

        RefundDto result = refundService.getRefundByBookingId(bookingId);

        assertNotNull(result);
        assertEquals(bookingId, result.getBookingId());
        assertEquals("PENDING", result.getRefundStatus());
    }

    @Test
    void testCreateRefundIsIdempotent() {
        // Calling createRefund twice on same booking should return same refund (not create duplicate)
        Long bookingId = createCancelledBooking();
        RefundDto first  = refundService.createRefund(bookingId);
        RefundDto second = refundService.createRefund(bookingId);

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.getRefundId(), second.getRefundId());
    }

    @Test
    void testRefundHasReason() {
        Long bookingId = createCancelledBooking();
        RefundDto refund = refundService.createRefund(bookingId);

        assertNotNull(refund.getReason());
        assertFalse(refund.getReason().isEmpty());
    }

}