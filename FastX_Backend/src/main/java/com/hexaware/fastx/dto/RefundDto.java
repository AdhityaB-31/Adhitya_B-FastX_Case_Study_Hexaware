package com.hexaware.fastx.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema
public class RefundDto {
    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long refundId;

    @Schema(example = "599.0")
    private Double refundAmount;

    @Schema(example = "2026-07-02T14:00:00")
    private LocalDateTime refundDate;

    @Schema(example = "PENDING",
            allowableValues = {"PENDING", "APPROVED", "REJECTED", "PROCESSED"})
    private String refundStatus;

    @Schema(example = "User cancelled confirmed booking")
    private String reason;

    @NotNull(message = "Booking ID is required")
    @Schema(example = "1")
    private Long bookingId;
}
