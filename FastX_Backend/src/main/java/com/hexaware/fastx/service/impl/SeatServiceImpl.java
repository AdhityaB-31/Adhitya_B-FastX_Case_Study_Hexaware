package com.hexaware.fastx.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.SeatDto;
import com.hexaware.fastx.entity.Booking;
import com.hexaware.fastx.entity.Seat;
import com.hexaware.fastx.enums.SeatStatus;
import com.hexaware.fastx.exception.ResourceNotFoundException;
import com.hexaware.fastx.exception.SeatNotAvailableException;
import com.hexaware.fastx.repository.BookingRepository;
import com.hexaware.fastx.repository.SeatRepository;
import com.hexaware.fastx.service.SeatService;

@Service
@Transactional
public class SeatServiceImpl implements SeatService {

    private static final Logger logger = LoggerFactory.getLogger(SeatServiceImpl.class);

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public void reserveSeats(List<Long> seatIds, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + seatId));

            if (seat.getSeatStatus() == SeatStatus.RESERVED) {
                throw new SeatNotAvailableException("Seat " + seat.getSeatNumber() + " is already reserved by another user");
            }
            if (seat.getSeatStatus() == SeatStatus.BOOKED) {
                throw new SeatNotAvailableException("Seat " + seat.getSeatNumber() + " is already booked");
            }

            seat.setSeatStatus(SeatStatus.RESERVED);
            seat.setReservedByBooking(booking);
            seatRepository.save(seat);

            logger.info("Seat reserved (held for payment): {} for Booking ID: {}", seat.getSeatNumber(), bookingId);
        }
    }

    @Override
    public void confirmSeats(List<Long> seatIds) {
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + seatId));

            if (seat.getSeatStatus() != SeatStatus.RESERVED) {
                logger.warn("Seat {} is not in RESERVED status, current status: {}", seat.getSeatNumber(), seat.getSeatStatus());
                continue;
            }

            seat.setSeatStatus(SeatStatus.BOOKED);
            seatRepository.save(seat);

            logger.info("Seat confirmed (BOOKED): {}", seat.getSeatNumber());
        }
    }

    @Override
    public void releaseSeats(List<Long> seatIds) {
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + seatId));

            seat.setSeatStatus(SeatStatus.AVAILABLE);
            seat.setReservedByBooking(null);
            seatRepository.save(seat);

            logger.info("Seat released: {}", seat.getSeatNumber());
        }
    }

    @Override
    public List<SeatDto> getSeatsByBus(Long busId) {
        List<Seat> seats = seatRepository.findSeatsByBusId(busId);
        List<SeatDto> seatDtos = new ArrayList<>();

        for (Seat seat : seats) {
            SeatDto dto = mapToDto(seat);
            seatDtos.add(dto);
        }

        return seatDtos;
    }

    @Override
    public List<SeatDto> getAvailableSeats(Long busId) {
        List<Seat> seats = seatRepository.findSeatsByBusIdAndStatus(busId, SeatStatus.AVAILABLE);
        List<SeatDto> seatDtos = new ArrayList<>();

        for (Seat seat : seats) {
            SeatDto dto = mapToDto(seat);
            seatDtos.add(dto);
        }

        return seatDtos;
    }

    private SeatDto mapToDto(Seat seat) {
        SeatDto dto = new SeatDto();
        dto.setSeatId(seat.getSeatId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setSeatType(seat.getSeatType() != null ? seat.getSeatType().name() : null);
        dto.setSeatStatus(seat.getSeatStatus() != null ? seat.getSeatStatus().name() : null);
        dto.setBusId(seat.getBus() != null ? seat.getBus().getBusId() : null);
        dto.setReservedByBookingId(seat.getReservedByBooking() != null ? seat.getReservedByBooking().getBookingId() : null);
        return dto;
    }
}
