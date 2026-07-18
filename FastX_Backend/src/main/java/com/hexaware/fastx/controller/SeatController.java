package com.hexaware.fastx.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hexaware.fastx.dto.SeatDto;
import com.hexaware.fastx.service.SeatService;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/reserve/{bookingId}")
    public ResponseEntity<String> reserveSeats(@RequestBody List<Long> seatIds,
                                                @PathVariable Long bookingId) {
        seatService.reserveSeats(seatIds, bookingId);
        return new ResponseEntity<>("Seats reserved successfully for Booking ID: " + bookingId, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('BUS_OPERATOR', 'ADMIN')")
    @PostMapping("/release")
    public ResponseEntity<String> releaseSeats(@RequestBody List<Long> seatIds) {
        seatService.releaseSeats(seatIds);
        return new ResponseEntity<>("Seats released successfully", HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<SeatDto>> getSeatsByBus(@PathVariable Long busId) {
        return new ResponseEntity<>(seatService.getSeatsByBus(busId), HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/bus/{busId}/available")
    public ResponseEntity<List<SeatDto>> getAvailableSeats(@PathVariable Long busId) {
        return new ResponseEntity<>(seatService.getAvailableSeats(busId), HttpStatus.OK);
    }
}
