package com.hexaware.fastx.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.BusOperatorDto;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BusOperatorServiceTest {

    @Autowired
    private BusOperatorService busOperatorService;

    @Test
    void testCreateOperator() {

        BusOperatorDto operator = new BusOperatorDto();
        operator.setCompanyName("FastX Travels");
        operator.setEmail("fastx@travels.com");
        operator.setPassword("password123");
        operator.setPhoneNumber("9876543210");
        operator.setAddress("Puducherry");

        BusOperatorDto result = busOperatorService.createOperator(operator);

        assertNotNull(result);
        assertNotNull(result.getOperatorId());

        assertEquals(
                "FastX Travels",
                result.getCompanyName());
    }

    @Test
    void testGetOperatorById() {

        BusOperatorDto operator = new BusOperatorDto();
        operator.setCompanyName("Buddy Travels");
        operator.setEmail("buddy@travels.com");
        operator.setPassword("password123");
        operator.setPhoneNumber("9876543211");
        operator.setAddress("Chennai");

        BusOperatorDto savedOperator = busOperatorService.createOperator(operator);

        BusOperatorDto result = busOperatorService.getOperatorById(
                savedOperator.getOperatorId());

        assertNotNull(result);

        assertEquals(
                savedOperator.getOperatorId(),
                result.getOperatorId());
    }

    @Test
    void testGetAllOperators() {

        BusOperatorDto operator = new BusOperatorDto();
        operator.setCompanyName("Adhitya Travels");
        operator.setEmail("adhitya@travels.com");
        operator.setPassword("password123");
        operator.setPhoneNumber("9876543212");
        operator.setAddress("Puducherry");

        busOperatorService.createOperator(operator);

        List<BusOperatorDto> operators = busOperatorService.getAllOperators();

        assertNotNull(operators);

        assertTrue(
                operators.size() > 0);
    }

    @Test
    void testUpdateOperator() {

        BusOperatorDto operator = new BusOperatorDto();
        operator.setCompanyName("Old FastX Travels");
        operator.setEmail("update@travels.com");
        operator.setPassword("password123");
        operator.setPhoneNumber("9876543213");
        operator.setAddress("Puducherry");

        BusOperatorDto savedOperator = busOperatorService.createOperator(operator);

        BusOperatorDto updatedOperator = new BusOperatorDto();

        updatedOperator.setCompanyName("New FastX Travels");
        updatedOperator.setPhoneNumber("9123456789");
        updatedOperator.setAddress("Chennai");

        BusOperatorDto result = busOperatorService.updateOperator(
                savedOperator.getOperatorId(),
                updatedOperator);

        assertEquals(
                "New FastX Travels",
                result.getCompanyName());

        assertEquals(
                "Chennai",
                result.getAddress());
    }

    @Test
    void testDeleteOperator() {

        BusOperatorDto operator = new BusOperatorDto();
        operator.setCompanyName("Delete FastX Travels");
        operator.setEmail("delete@travels.com");
        operator.setPassword("password123");
        operator.setPhoneNumber("9876543214");
        operator.setAddress("Puducherry");

        BusOperatorDto savedOperator = busOperatorService.createOperator(operator);

        busOperatorService.deleteOperator(
                savedOperator.getOperatorId());

        assertThrows(
                RuntimeException.class,
                () -> busOperatorService.getOperatorById(
                        savedOperator.getOperatorId()));
    }
    @Test
    void testGetOperatorByEmail() {

        BusOperatorDto operator = new BusOperatorDto();
        operator.setCompanyName("Email Test Travels");
        operator.setEmail("emailtest@travels.com");
        operator.setPassword("password123");
        operator.setPhoneNumber("9876543299");
        operator.setAddress("Puducherry");

        BusOperatorDto savedOperator = busOperatorService.createOperator(operator);

        BusOperatorDto result = busOperatorService.getOperatorByEmail("emailtest@travels.com");

        assertNotNull(result);
        assertEquals(savedOperator.getOperatorId(), result.getOperatorId());
    }

    @Test
    void testGetActiveOperators() {

        List<BusOperatorDto> operators = busOperatorService.getActiveOperators();

        assertNotNull(operators);
        // Assuming some might be active, or none.
    }

    @Test
    void testSearchOperatorsByName() {

        BusOperatorDto operator = new BusOperatorDto();
        operator.setCompanyName("Search Travels");
        operator.setEmail("search@travels.com");
        operator.setPassword("password123");
        operator.setPhoneNumber("9876543298");
        operator.setAddress("Puducherry");

        busOperatorService.createOperator(operator);

        List<BusOperatorDto> results = busOperatorService.searchOperatorsByName("Search");

        assertNotNull(results);
        assertTrue(results.size() > 0);
    }
}