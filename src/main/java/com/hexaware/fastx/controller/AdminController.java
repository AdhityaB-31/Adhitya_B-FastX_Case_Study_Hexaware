package com.hexaware.fastx.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.fastx.dto.BookingDto;
import com.hexaware.fastx.dto.BusOperatorDto;
import com.hexaware.fastx.dto.UserDto;
import com.hexaware.fastx.repository.BookingRepository;
import com.hexaware.fastx.repository.BusOperatorRepository;
import com.hexaware.fastx.repository.BusRepository;
import com.hexaware.fastx.repository.UserRepository;
import com.hexaware.fastx.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")   // <-- class-level: protects ALL methods below
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusOperatorRepository operatorRepository;
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        return new ResponseEntity<>(adminService.getDashboardStatistics(), HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> manageUsers() {
        return new ResponseEntity<>(adminService.manageUsers(), HttpStatus.OK);
    }

    @GetMapping("/operators")
    public ResponseEntity<List<BusOperatorDto>> manageOperators() {
        return new ResponseEntity<>(adminService.manageOperators(), HttpStatus.OK);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingDto>> manageBookings() {
        return new ResponseEntity<>(adminService.manageBookings(), HttpStatus.OK);
    }
    
    @GetMapping("/dashboard/stats")
    public Map<String, Long> getDashboardStats() {

        Map<String, Long> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalOperators", operatorRepository.count());
        stats.put("totalBuses", busRepository.count());
        stats.put("totalBookings", bookingRepository.count());

        return stats;
    }

    @PutMapping("/operators/{operatorId}/approve")
    public ResponseEntity<BusOperatorDto> approveOperator(@PathVariable Long operatorId) {
        return new ResponseEntity<>(adminService.approveOperator(operatorId), HttpStatus.OK);
    }

    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(
            @PathVariable Long userId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String reason) {
        try {
            return new ResponseEntity<>(adminService.toggleUserStatus(userId, reason), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/operators/{operatorId}/toggle-status")
    public ResponseEntity<?> toggleOperatorStatus(
            @PathVariable Long operatorId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String reason) {
        try {
            return new ResponseEntity<>(adminService.toggleOperatorStatus(operatorId, reason), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        }
    }
}
