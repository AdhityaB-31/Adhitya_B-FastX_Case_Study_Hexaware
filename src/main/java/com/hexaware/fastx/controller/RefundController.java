package com.hexaware.fastx.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hexaware.fastx.dto.RefundDto;
import com.hexaware.fastx.enums.RefundStatus;
import com.hexaware.fastx.service.RefundService;
import com.hexaware.fastx.security.FastXUserDetails;
import com.hexaware.fastx.exception.UnauthorizedException;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<RefundDto> createRefund(@PathVariable Long bookingId) {
        return new ResponseEntity<>(refundService.createRefund(bookingId), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BUS_OPERATOR')")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<RefundDto> getRefundByBookingId(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {
        // If it's an operator, make sure they own the booking/bus
        if (hasRole(loggedInUser, "ROLE_BUS_OPERATOR")) {
            RefundDto refund = refundService.getRefundByBookingId(bookingId);
            refundService.verifyOperatorRefundOwnership(refund.getRefundId(), loggedInUser.getEntityId());
        }
        return new ResponseEntity<>(refundService.getRefundByBookingId(bookingId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('BUS_OPERATOR')")
    @PutMapping("/{refundId}/approve")
    public ResponseEntity<RefundDto> approveRefund(
            @PathVariable Long refundId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {
        refundService.verifyOperatorRefundOwnership(refundId, loggedInUser.getEntityId());
        return new ResponseEntity<>(refundService.approveRefund(refundId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('BUS_OPERATOR')")
    @PutMapping("/{refundId}/reject")
    public ResponseEntity<RefundDto> rejectRefund(
            @PathVariable Long refundId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {
        refundService.verifyOperatorRefundOwnership(refundId, loggedInUser.getEntityId());
        return new ResponseEntity<>(refundService.rejectRefund(refundId), HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{refundId}")
    public ResponseEntity<RefundDto> getRefundStatus(
            @PathVariable Long refundId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {
        if (hasRole(loggedInUser, "ROLE_BUS_OPERATOR")) {
            refundService.verifyOperatorRefundOwnership(refundId, loggedInUser.getEntityId());
        }
        return new ResponseEntity<>(refundService.getRefundStatus(refundId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('BUS_OPERATOR')")
    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<RefundDto>> getRefundsByOperator(
            @PathVariable Long operatorId,
            @AuthenticationPrincipal FastXUserDetails loggedInUser) {
        if (!loggedInUser.getEntityId().equals(operatorId)) {
            throw new UnauthorizedException("You can only view your own refunds.");
        }
        return new ResponseEntity<>(refundService.getRefundsByOperator(operatorId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status")
    public ResponseEntity<List<RefundDto>> getRefundsByStatus(@RequestParam RefundStatus status) {
        return new ResponseEntity<>(refundService.getRefundsByStatus(status), HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getall")
    public ResponseEntity<List<RefundDto>> getAllRefunds() {
        return new ResponseEntity<>(refundService.getAllRefunds(), HttpStatus.OK);
    }

    private boolean hasRole(FastXUserDetails user, String role) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
