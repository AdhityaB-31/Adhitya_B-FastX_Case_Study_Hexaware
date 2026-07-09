package com.hexaware.fastx.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.BookingDto;
import com.hexaware.fastx.dto.BusOperatorDto;
import com.hexaware.fastx.dto.UserDto;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Test
    void testManageUsers() {
        List<UserDto> users = adminService.manageUsers();
        assert users != null : "User list should not be null";
    }

    @Test
    void testManageOperators() {
        List<BusOperatorDto> operators = adminService.manageOperators();
        assert operators != null : "Operator list should not be null";
    }

    @Test
    void testManageBookings() {
        List<BookingDto> bookings = adminService.manageBookings();
        assert bookings != null : "Booking list should not be null";
    }

    @Test
    void testGetDashboardStatistics() {
        java.util.Map<String, Object> stats = adminService.getDashboardStatistics();
        assert stats != null : "Dashboard statistics should not be null";
        assert stats.containsKey("totalUsers") : "Should contain totalUsers";
        assert stats.containsKey("totalBookings") : "Should contain totalBookings";
    }
}
