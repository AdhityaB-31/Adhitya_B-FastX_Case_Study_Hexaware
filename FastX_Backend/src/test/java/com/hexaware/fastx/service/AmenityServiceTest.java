package com.hexaware.fastx.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.AmenityDto;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AmenityServiceTest {

    @Autowired
    private AmenityService amenityService;

    @Test
    void testCreateAmenity() {

        // Creating a bus amenity
        AmenityDto amenity = new AmenityDto();
        amenity.setAmenityName("FastX WiFi");

        AmenityDto result = amenityService.createAmenity(amenity);

        assertNotNull(result);
        assertNotNull(result.getAmenityId());
        assertEquals("FastX WiFi", result.getAmenityName());
    }

    @Test
    void testGetAmenityById() {

        // Save amenity first
        AmenityDto amenity = new AmenityDto();
        amenity.setAmenityName("Mobile Charging");

        AmenityDto savedAmenity = amenityService.createAmenity(amenity);

        // Fetch using ID
        AmenityDto result = amenityService.getAmenityById(savedAmenity.getAmenityId());

        assertNotNull(result);
        assertEquals("Mobile Charging", result.getAmenityName());
    }

    @Test
    void testGetAllAmenities() {

        AmenityDto wifi = new AmenityDto();
        wifi.setAmenityName("FastX WiFi");
        amenityService.createAmenity(wifi);

        AmenityDto charging = new AmenityDto();
        charging.setAmenityName("Charging Point");
        amenityService.createAmenity(charging);

        List<AmenityDto> amenityList = amenityService.getAllAmenities();

        assertNotNull(amenityList);
        assertTrue(amenityList.size() >= 2);
    }

    @Test
    void testUpdateAmenity() {

        AmenityDto amenity = new AmenityDto();
        amenity.setAmenityName("Basic Seat");

        AmenityDto savedAmenity = amenityService.createAmenity(amenity);

        AmenityDto updatedAmenity = new AmenityDto();
        updatedAmenity.setAmenityName("Luxury Recliner Seat");

        AmenityDto result = amenityService.updateAmenity(savedAmenity.getAmenityId(), updatedAmenity);

        assertEquals("Luxury Recliner Seat", result.getAmenityName());
    }

    @Test
    void testDeleteAmenity() {

        AmenityDto amenity = new AmenityDto();
        amenity.setAmenityName("FastX TV");

        AmenityDto savedAmenity = amenityService.createAmenity(amenity);

        // Delete amenity
        amenityService.deleteAmenity(savedAmenity.getAmenityId());

        // Verify deletion
        assertThrows(
                RuntimeException.class,
                () -> amenityService.getAmenityById(savedAmenity.getAmenityId()));
    }
    @Test
    void testGetAmenityByName() {

        AmenityDto amenity = new AmenityDto();
        amenity.setAmenityName("Water Bottle");
        amenityService.createAmenity(amenity);

        AmenityDto result = amenityService.getAmenityByName("Water Bottle");

        assertNotNull(result);
        assertEquals("Water Bottle", result.getAmenityName());
    }

    @Test
    void testSearchAmenitiesByName() {

        AmenityDto amenity1 = new AmenityDto();
        amenity1.setAmenityName("AC Sleeper");
        amenityService.createAmenity(amenity1);

        AmenityDto amenity2 = new AmenityDto();
        amenity2.setAmenityName("AC Seater");
        amenityService.createAmenity(amenity2);

        List<AmenityDto> results = amenityService.searchAmenitiesByName("AC");

        assertNotNull(results);
        assertTrue(results.size() >= 2);
    }
}