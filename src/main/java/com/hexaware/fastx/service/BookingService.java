package com.hexaware.fastx.service;

import java.util.List;

import com.hexaware.fastx.dto.BookingDto;

public interface BookingService {
    BookingDto createBooking(BookingDto bookingDto);
    BookingDto confirmBooking(Long bookingId);
    BookingDto cancelBooking(Long bookingId);
    BookingDto cancelBooking(Long bookingId, String reason);
    BookingDto getBookingById(Long bookingId);
    List<BookingDto> getBookingsByUser(Long userId);
    List<BookingDto> getBookingsByBus(Long busId);
    Double calculateFare(Long busId, int numberOfSeats);

    /**
     * Verifies that the given booking belongs to the specified user.
     * Throws UnauthorizedException if the booking belongs to a different user.
     * Used by controllers to enforce USER ownership before confirm/cancel.
     */
    void verifyBookingOwnership(Long bookingId, Long userId);

    /**
     * Verifies that the given bus belongs to the specified operator.
     * Throws UnauthorizedException if the bus belongs to a different operator.
     */
    void verifyBusOperatorOwnership(Long busId, Long operatorId);
}
