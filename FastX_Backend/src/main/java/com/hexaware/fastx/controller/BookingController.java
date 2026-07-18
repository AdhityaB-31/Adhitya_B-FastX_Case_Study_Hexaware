package com.hexaware.fastx.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hexaware.fastx.dto.BookingDto;
import com.hexaware.fastx.exception.UnauthorizedException;
import com.hexaware.fastx.security.FastXUserDetails;
import com.hexaware.fastx.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<BookingDto> createBooking(
            @Valid @RequestBody BookingDto bookingDto,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (!hasRole(loggedInUser, "ROLE_ADMIN") && !loggedInUser.getEntityId().equals(bookingDto.getUserId())) {
            throw new UnauthorizedException("You can only create bookings for your own account.");
        }

        return new ResponseEntity<>(bookingService.createBooking(bookingDto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingDto> confirmBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (!hasRole(loggedInUser, "ROLE_ADMIN")) {
            bookingService.verifyBookingOwnership(bookingId, loggedInUser.getEntityId());
        }

        return new ResponseEntity<>(bookingService.confirmBooking(bookingId), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingDto> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (!hasRole(loggedInUser, "ROLE_ADMIN")) {
            bookingService.verifyBookingOwnership(bookingId, loggedInUser.getEntityId());
        }

        return new ResponseEntity<>(bookingService.cancelBooking(bookingId, reason), HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {
        BookingDto booking = bookingService.getBookingById(bookingId);
        if (hasRole(loggedInUser, "ROLE_BUS_OPERATOR")) {
            bookingService.verifyBusOperatorOwnership(booking.getBusId(), loggedInUser.getEntityId());
        } else if (hasRole(loggedInUser, "ROLE_USER")) {
            if (!booking.getUserId().equals(loggedInUser.getEntityId())) {
                throw new UnauthorizedException("You can only view your own bookings.");
            }
        } else if (!hasRole(loggedInUser, "ROLE_ADMIN")) {
            throw new UnauthorizedException("Access Denied: Admin role does not manage bookings.");
        }
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDto>> getBookingsByUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (!hasRole(loggedInUser, "ROLE_ADMIN") && !loggedInUser.getEntityId().equals(userId)) {
            throw new UnauthorizedException("You can only view your own bookings.");
        }

        return new ResponseEntity<>(bookingService.getBookingsByUser(userId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('BUS_OPERATOR')")
    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<BookingDto>> getBookingsByBus(
            @PathVariable Long busId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {
        bookingService.verifyBusOperatorOwnership(busId, loggedInUser.getEntityId());
        return new ResponseEntity<>(bookingService.getBookingsByBus(busId), HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/fare")
    public ResponseEntity<Double> calculateFare(@RequestParam Long busId, @RequestParam int numberOfSeats) {
        return new ResponseEntity<>(bookingService.calculateFare(busId, numberOfSeats), HttpStatus.OK);
    }

    private boolean hasRole(FastXUserDetails user, String role) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
