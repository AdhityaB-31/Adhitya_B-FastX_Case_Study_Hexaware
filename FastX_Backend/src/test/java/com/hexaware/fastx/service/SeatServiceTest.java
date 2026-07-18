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
import com.hexaware.fastx.dto.RoleDto;
import com.hexaware.fastx.dto.SeatDto;
import com.hexaware.fastx.dto.UserDto;
import com.hexaware.fastx.entity.Seat;
import com.hexaware.fastx.enums.SeatStatus;
import com.hexaware.fastx.repository.SeatRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SeatServiceTest {

    @Autowired
    private SeatService seatService;

    @Autowired
    private BusService busService;

    @Autowired
    private BusOperatorService busOperatorService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SeatRepository seatRepository;

    // Create sample data required for seat testing
    private Long[] createTestData() {

        // Create Bus Operator
        BusOperatorDto operator = new BusOperatorDto();
        operator.setCompanyName("FastX Travels");
        operator.setEmail("seat" + System.nanoTime() + "@fastx.com");
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
        bus.setFare(600.0);
        bus.setTotalSeats(40);
        bus.setOperatorId(savedOperator.getOperatorId());

        BusDto savedBus = busService.addBus(bus);

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

        // Get Seats
        List<SeatDto> seats = seatService.getSeatsByBus(savedBus.getBusId());

        Long firstSeatId = seats.get(0).getSeatId();
        Long secondSeatId = seats.get(1).getSeatId();

        // Create Passenger
        PassengerDto passenger = new PassengerDto();
        passenger.setName("Buddy");
        passenger.setAge(21);
        passenger.setGender("MALE");
        passenger.setSeatId(secondSeatId);

        List<PassengerDto> passengers = new ArrayList<>();
        passengers.add(passenger);

        // Create Booking
        BookingDto booking = new BookingDto();
        booking.setUserId(savedUser.getUserId());
        booking.setBusId(savedBus.getBusId());
        booking.setPassengers(passengers);

        BookingDto savedBooking = bookingService.createBooking(booking);

        return new Long[] {
                savedBus.getBusId(),
                firstSeatId,
                savedBooking.getBookingId()
        };
    }

    @Test
    void testGetSeatsByBus() {

        Long[] ids = createTestData();

        List<SeatDto> seats = seatService.getSeatsByBus(ids[0]);

        assertNotNull(seats);
        assertEquals(40, seats.size());
    }

    @Test
    void testGetAvailableSeats() {

        Long[] ids = createTestData();

        List<SeatDto> seats = seatService.getAvailableSeats(ids[0]);

        assertNotNull(seats);
        assertEquals(39, seats.size());
    }

    @Test
    void testReserveSeats() {

        Long[] ids = createTestData();

        List<Long> seatIds = new ArrayList<>();

        seatIds.add(ids[1]);

        seatService.reserveSeats(
                seatIds,
                ids[2]);

        Seat seat = seatRepository.findById(ids[1]).get();

        assertEquals(
                SeatStatus.RESERVED,
                seat.getSeatStatus());
    }

    @Test
    void testConfirmSeats() {

        Long[] ids = createTestData();

        List<Long> seatIds = new ArrayList<>();

        seatIds.add(ids[1]);

        seatService.reserveSeats(
                seatIds,
                ids[2]);

        seatService.confirmSeats(seatIds);

        Seat seat = seatRepository.findById(ids[1]).get();

        assertEquals(
                SeatStatus.BOOKED,
                seat.getSeatStatus());
    }

    @Test
    void testReleaseSeats() {

        Long[] ids = createTestData();

        List<Long> seatIds = new ArrayList<>();

        seatIds.add(ids[1]);

        seatService.reserveSeats(
                seatIds,
                ids[2]);

        seatService.releaseSeats(seatIds);

        Seat seat = seatRepository.findById(ids[1]).get();

        assertEquals(
                SeatStatus.AVAILABLE,
                seat.getSeatStatus());
    }
}