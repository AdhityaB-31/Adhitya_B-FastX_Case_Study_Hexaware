package com.hexaware.fastx.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hexaware.fastx.dto.BusDto;
import com.hexaware.fastx.dto.SeatDto;
import com.hexaware.fastx.enums.BusType;
import com.hexaware.fastx.exception.UnauthorizedException;
import com.hexaware.fastx.security.FastXUserDetails;
import com.hexaware.fastx.service.BusService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/buses")
public class BusController {

    @Autowired
    private BusService busService;

    @PreAuthorize("hasAnyRole('BUS_OPERATOR', 'ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<BusDto> addBus(
            @Valid @RequestBody BusDto busDto,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (hasRole(loggedInUser, "ROLE_BUS_OPERATOR")
                && busDto.getOperatorId() != null
                && !loggedInUser.getEntityId().equals(busDto.getOperatorId())) {
            throw new UnauthorizedException("You can only add buses under your own operator account.");
        }

        return new ResponseEntity<>(busService.addBus(busDto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('BUS_OPERATOR', 'ADMIN')")
    @PutMapping("/update/{busId}")
    public ResponseEntity<BusDto> updateBus(
            @PathVariable Long busId,
            @Valid @RequestBody BusDto busDto,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (hasRole(loggedInUser, "ROLE_BUS_OPERATOR")) {
            busService.verifyBusOwnership(busId, loggedInUser.getEntityId());
        }

        return new ResponseEntity<>(busService.updateBus(busId, busDto), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('BUS_OPERATOR', 'ADMIN')")
    @DeleteMapping("/delete/{busId}")
    public ResponseEntity<Void> deleteBus(
            @PathVariable Long busId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (hasRole(loggedInUser, "ROLE_BUS_OPERATOR")) {
            busService.verifyBusOwnership(busId, loggedInUser.getEntityId());
        }

        busService.deleteBus(busId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasAnyRole('BUS_OPERATOR', 'ADMIN')")
    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<BusDto>> getBusesByOperator(
            @PathVariable Long operatorId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {

        if (hasRole(loggedInUser, "ROLE_BUS_OPERATOR") && !loggedInUser.getEntityId().equals(operatorId)) {
            throw new UnauthorizedException("You can only view buses belonging to your own operator account.");
        }

        return new ResponseEntity<>(busService.getBusesByOperator(operatorId), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('BUS_OPERATOR', 'ADMIN')")
    @GetMapping("/search/name")
    public ResponseEntity<List<BusDto>> searchBusesByName(@RequestParam String name) {
        return new ResponseEntity<>(busService.searchBusesByName(name), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BusDto>> getAllBuses() {
        return new ResponseEntity<>(busService.getAllBuses(), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BusDto>> searchBus(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate journeyDate) {
        return new ResponseEntity<>(busService.searchBus(origin, destination, journeyDate), HttpStatus.OK);
    }

    @GetMapping("/type")
    public ResponseEntity<List<BusDto>> getBusesByType(@RequestParam BusType busType) {
        return new ResponseEntity<>(busService.getBusesByType(busType), HttpStatus.OK);
    }

    @GetMapping("/fare")
    public ResponseEntity<List<BusDto>> getBusesByFare(@RequestParam Double min, @RequestParam Double max) {
        return new ResponseEntity<>(busService.getBusesByFare(min, max), HttpStatus.OK);
    }

    @GetMapping("/cheapest")
    public ResponseEntity<List<BusDto>> getCheapestBuses() {
        return new ResponseEntity<>(busService.getCheapestBuses(), HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{busId}")
    public ResponseEntity<BusDto> getBusDetails(@PathVariable Long busId) {
        return new ResponseEntity<>(busService.getBusDetails(busId), HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{busId}/available-seats")
    public ResponseEntity<List<SeatDto>> getAvailableSeats(@PathVariable Long busId) {
        return new ResponseEntity<>(busService.getAvailableSeats(busId), HttpStatus.OK);
    }

    private boolean hasRole(FastXUserDetails user, String role) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
