package com.hexaware.fastx.entity;

import java.time.LocalDateTime;

import com.hexaware.fastx.enums.RefundStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    private Double refundAmount;

    /** Why was this refund created — e.g. "User cancelled confirmed booking" */
    private String reason;

    private LocalDateTime refundDate;

    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus = RefundStatus.PENDING;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
