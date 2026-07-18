package com.hexaware.fastx.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hexaware.fastx.entity.Seat;
import com.hexaware.fastx.enums.SeatStatus;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT s FROM Seat s WHERE s.bus.busId = :busId")
    List<Seat> findSeatsByBusId(@Param("busId") Long busId);

    @Query("SELECT s FROM Seat s WHERE s.bus.busId = :busId AND s.seatStatus = :status")
    List<Seat> findSeatsByBusIdAndStatus(@Param("busId") Long busId, @Param("status") SeatStatus status);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.bus.busId = :busId AND s.seatStatus = :status")
    Long countSeatsByBusIdAndStatus(@Param("busId") Long busId, @Param("status") SeatStatus status);
}
