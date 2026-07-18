package com.hexaware.fastx.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hexaware.fastx.dto.BookingDto;
import com.hexaware.fastx.dto.UserDto;
import com.hexaware.fastx.exception.UnauthorizedException;
import com.hexaware.fastx.security.FastXUserDetails;
import com.hexaware.fastx.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active")
    public ResponseEntity<List<UserDto>> getActiveUsers() {
        return new ResponseEntity<>(userService.getActiveUsers(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsersByName(@RequestParam String name) {
        return new ResponseEntity<>(userService.searchUsersByName(name), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long userId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (!hasRole(loggedInUser, "ROLE_ADMIN") && !loggedInUser.getEntityId().equals(userId)) {
            throw new UnauthorizedException("You can only view your own profile.");
        }

        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/update/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserDto userDto,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (!hasRole(loggedInUser, "ROLE_ADMIN") && !loggedInUser.getEntityId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own profile.");
        }

        return new ResponseEntity<>(userService.updateUser(userId, userDto), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(
            @PathVariable String email,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (!hasRole(loggedInUser, "ROLE_ADMIN") && !loggedInUser.getUsername().equals(email)) {
            throw new UnauthorizedException("You can only look up your own account.");
        }

        return new ResponseEntity<>(userService.getUserByEmail(email), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{userId}/bookings")
    public ResponseEntity<List<BookingDto>> getBookingHistory(
            @PathVariable Long userId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (!hasRole(loggedInUser, "ROLE_ADMIN") && !loggedInUser.getEntityId().equals(userId)) {
            throw new UnauthorizedException("You can only view your own booking history.");
        }

        return new ResponseEntity<>(userService.getBookingHistory(userId), HttpStatus.OK);
    }

    private boolean hasRole(FastXUserDetails user, String role) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
