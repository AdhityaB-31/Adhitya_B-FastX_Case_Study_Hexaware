package com.hexaware.fastx.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hexaware.fastx.entity.Passenger;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    @Query("SELECT p FROM Passenger p WHERE p.booking.bookingId = :bookingId")
    List<Passenger> findPassengersByBookingId(@Param("bookingId") Long bookingId);

    @Query("SELECT COUNT(p) FROM Passenger p WHERE p.booking.bookingId = :bookingId")
    Long countPassengersByBookingId(@Param("bookingId") Long bookingId);
}
