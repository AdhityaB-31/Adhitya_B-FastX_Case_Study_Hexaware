package com.hexaware.fastx.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hexaware.fastx.entity.Booking;
import com.hexaware.fastx.enums.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId")
    List<Booking> findBookingsByUserId(@Param("userId") Long userId);

    List<Booking> findByBookingStatus(BookingStatus bookingStatus);

    List<Booking> findByBookingStatusAndReservationExpiresAtBefore(BookingStatus status, java.time.LocalDateTime time);

    List<Booking> findByBookingDateBetween(LocalDate startDate, LocalDate endDate);

    Long countByBookingStatus(BookingStatus bookingStatus);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.userId = :userId")
    Long countBookingsByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.bus.busId = :busId")
    List<Booking> findBookingsByBusId(@Param("busId") Long busId);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.bus.busId = :busId AND b.bookingStatus = 'CONFIRMED'")
    Double calculateBusRevenue(@Param("busId") Long busId);

    @Query("""
        SELECT b.bus.origin, b.bus.destination, COUNT(b.bookingId)
        FROM Booking b
        GROUP BY b.bus.origin, b.bus.destination
        ORDER BY COUNT(b.bookingId) DESC
        """)
    List<Object[]> findPopularRoutes();
}
