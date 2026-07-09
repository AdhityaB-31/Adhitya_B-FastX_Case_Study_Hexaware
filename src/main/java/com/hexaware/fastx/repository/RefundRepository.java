package com.hexaware.fastx.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hexaware.fastx.entity.Refund;
import com.hexaware.fastx.enums.RefundStatus;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    @Query("SELECT r FROM Refund r WHERE r.booking.bookingId = :bookingId")
    Optional<Refund> findByBookingId(@Param("bookingId") Long bookingId);

    List<Refund> findByRefundStatus(RefundStatus refundStatus);

    @Query("SELECT r FROM Refund r WHERE r.booking.bus.busOperator.operatorId = :operatorId")
    List<Refund> findByOperatorId(@Param("operatorId") Long operatorId);
}
