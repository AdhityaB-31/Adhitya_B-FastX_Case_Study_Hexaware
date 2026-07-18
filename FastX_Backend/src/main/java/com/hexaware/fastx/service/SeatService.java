package com.hexaware.fastx.service;

import java.util.List;

import com.hexaware.fastx.dto.SeatDto;

public interface SeatService {
    void reserveSeats(List<Long> seatIds, Long bookingId);
    void confirmSeats(List<Long> seatIds);
    void releaseSeats(List<Long> seatIds);
    List<SeatDto> getSeatsByBus(Long busId);
    List<SeatDto> getAvailableSeats(Long busId);
}
