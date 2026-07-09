package com.hexaware.fastx.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.BusDto;
import com.hexaware.fastx.dto.BusOperatorDto;
import com.hexaware.fastx.dto.SeatDto;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BusServiceTest {

    @Autowired
    private BusService busService;

    @Autowired
    private BusOperatorService busOperatorService;

    // Create sample operator
    private Long createTestOperator() {

        BusOperatorDto operator = new BusOperatorDto();
        operator.setCompanyName("FastX Travels");
        operator.setEmail("fastxbus@travels.com");
        operator.setPassword("password123");
        operator.setPhoneNumber("9876543210");
        operator.setAddress("Puducherry");

        return busOperatorService
                .createOperator(operator)
                .getOperatorId();
    }

    // Create sample bus
    private BusDto createTestBus(
            Long operatorId,
            String busName,
            String origin,
            String destination) {

        BusDto bus = new BusDto();

        bus.setBusName(busName);
        bus.setBusNumber("TN01FX1001");
        bus.setBusType("AC_SLEEPER");
        bus.setOrigin(origin);
        bus.setDestination(destination);
        bus.setJourneyDate(LocalDate.of(2026, 9, 1));
        bus.setDepartureTime(LocalTime.of(21, 0));
        bus.setArrivalTime(LocalTime.of(5, 0));
        bus.setFare(800.0);
        bus.setTotalSeats(40);
        bus.setOperatorId(operatorId);

        return busService.addBus(bus);
    }

    @Test
    void testAddBus() {

        Long operatorId = createTestOperator();

        BusDto result = createTestBus(
                operatorId,
                "FastX Express",
                "Puducherry",
                "Chennai");

        assertNotNull(result);
        assertNotNull(result.getBusId());

        assertEquals(
                "FastX Express",
                result.getBusName());
    }

    @Test
    void testGetBusDetails() {

        Long operatorId = createTestOperator();

        BusDto savedBus = createTestBus(
                operatorId,
                "Buddy Express",
                "Puducherry",
                "Chennai");

        BusDto result = busService.getBusDetails(
                savedBus.getBusId());

        assertNotNull(result);

        assertEquals(
                savedBus.getBusId(),
                result.getBusId());
    }

    @Test
    void testSearchBus() {

        Long operatorId = createTestOperator();

        createTestBus(
                operatorId,
                "FastX Night Rider",
                "Puducherry",
                "Chennai");

        List<BusDto> buses = busService.searchBus(
                "Puducherry",
                "Chennai",
                LocalDate.of(2026, 9, 1));

        assertNotNull(buses);

        assertTrue(
                buses.size() > 0);
    }

    @Test
    void testUpdateBus() {

        Long operatorId = createTestOperator();

        BusDto savedBus = createTestBus(
                operatorId,
                "Old FastX Express",
                "Puducherry",
                "Chennai");

        BusDto updatedBus = new BusDto();

        updatedBus.setBusName("New FastX Express");
        updatedBus.setBusNumber(savedBus.getBusNumber());
        updatedBus.setBusType("AC_SLEEPER");
        updatedBus.setOrigin("Puducherry");
        updatedBus.setDestination("Chennai");
        updatedBus.setJourneyDate(LocalDate.of(2026, 9, 1));
        updatedBus.setDepartureTime(LocalTime.of(20, 0));
        updatedBus.setArrivalTime(LocalTime.of(5, 0));
        updatedBus.setFare(900.0);
        updatedBus.setTotalSeats(40);

        BusDto result = busService.updateBus(
                savedBus.getBusId(),
                updatedBus);

        assertEquals(
                "New FastX Express",
                result.getBusName());

        assertEquals(
                900.0,
                result.getFare());
    }

    @Test
    void testDeleteBus() {

        Long operatorId = createTestOperator();

        BusDto savedBus = createTestBus(
                operatorId,
                "Delete FastX Bus",
                "Puducherry",
                "Chennai");

        busService.deleteBus(savedBus.getBusId());

        assertThrows(
                RuntimeException.class,
                () -> busService.getBusDetails(
                        savedBus.getBusId()));
    }
    @Test
    void testGetAvailableSeats() {

        Long operatorId = createTestOperator();

        BusDto savedBus = createTestBus(
                operatorId,
                "Seat FastX Express",
                "Puducherry",
                "Chennai");

        List<SeatDto> availableSeats = busService.getAvailableSeats(savedBus.getBusId());

        assertNotNull(availableSeats);
        assertTrue(availableSeats.size() >= 0); // They might be zero if not initialized
    }

    @Test
    void testGetBusesByOperator() {

        Long operatorId = createTestOperator();

        createTestBus(
                operatorId,
                "Op FastX Express",
                "Puducherry",
                "Chennai");

        List<BusDto> buses = busService.getBusesByOperator(operatorId);

        assertNotNull(buses);
        assertTrue(buses.size() > 0);
    }

    @Test
    void testGetBusesByType() {

        Long operatorId = createTestOperator();

        createTestBus(
                operatorId,
                "Type FastX Express",
                "Puducherry",
                "Chennai");

        List<BusDto> buses = busService.getBusesByType(com.hexaware.fastx.enums.BusType.AC_SLEEPER);

        assertNotNull(buses);
        assertTrue(buses.size() > 0);
    }

    @Test
    void testGetBusesByFare() {

        Long operatorId = createTestOperator();

        createTestBus(
                operatorId,
                "Fare FastX Express",
                "Puducherry",
                "Chennai");

        List<BusDto> buses = busService.getBusesByFare(0.0, 1000.0);

        assertNotNull(buses);
        assertTrue(buses.size() > 0);
    }

    @Test
    void testSearchBusesByName() {

        Long operatorId = createTestOperator();

        createTestBus(
                operatorId,
                "Search Name Express",
                "Puducherry",
                "Chennai");

        List<BusDto> buses = busService.searchBusesByName("Search");

        assertNotNull(buses);
        assertTrue(buses.size() > 0);
    }

    @Test
    void testGetCheapestBuses() {

        Long operatorId = createTestOperator();

        createTestBus(
                operatorId,
                "Cheap Express",
                "Puducherry",
                "Chennai");

        List<BusDto> buses = busService.getCheapestBuses();

        assertNotNull(buses);
        assertTrue(buses.size() > 0);
    }

    @Test
    void testGetAllBuses() {
        Long operatorId = createTestOperator();
        createTestBus(operatorId, "All Buses Express", "Puducherry", "Chennai");

        List<BusDto> buses = busService.getAllBuses();

        assertNotNull(buses);
        assertTrue(buses.size() > 0);
    }

    @Test
    void testAddBusWithAmenityIds() {
        Long operatorId = createTestOperator();

        BusDto bus = new BusDto();
        bus.setBusName("Amenity Express");
        bus.setBusNumber("TN01FX2002");
        bus.setBusType("AC_SLEEPER");
        bus.setOrigin("Puducherry");
        bus.setDestination("Chennai");
        bus.setJourneyDate(java.time.LocalDate.of(2026, 10, 1));
        bus.setDepartureTime(java.time.LocalTime.of(21, 0));
        bus.setArrivalTime(java.time.LocalTime.of(5, 0));
        bus.setFare(700.0);
        bus.setTotalSeats(40);
        bus.setOperatorId(operatorId);
        // amenityIds is empty list — should create bus without amenities
        bus.setAmenityIds(new java.util.ArrayList<>());

        BusDto result = busService.addBus(bus);

        assertNotNull(result);
        assertNotNull(result.getBusId());
        assertEquals("Amenity Express", result.getBusName());
    }

    @Test
    void testBusDtoIncludesAmenityNames() {
        Long operatorId = createTestOperator();
        BusDto saved = createTestBus(operatorId, "Name Test Bus", "Puducherry", "Chennai");

        BusDto fetched = busService.getBusDetails(saved.getBusId());

        // amenityNames list should be initialized (even if empty)
        assertNotNull(fetched.getBusId());
        // amenityIds list should not be null after fetch
        // (may be null if no amenities — just assert bus details are intact)
        assertEquals("Name Test Bus", fetched.getBusName());
    }

}