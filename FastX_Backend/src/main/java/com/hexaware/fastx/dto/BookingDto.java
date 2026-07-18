package com.hexaware.fastx.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema
public class BookingDto {
    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long bookingId;

    @Schema(example = "2026-07-01", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate bookingDate;

    @Schema(example = "PENDING", allowableValues = { "PENDING", "CONFIRMED", "CANCELLED",
            "EXPIRED" }, accessMode = Schema.AccessMode.READ_ONLY)
    private String bookingStatus;

    @Schema(example = "2026-07-01T10:30:00Z", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime reservationExpiresAt;

    @Schema(example = "1198.0", accessMode = Schema.AccessMode.READ_ONLY)
    private Double totalAmount;

    @Schema(example = "2", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer numberOfSeats;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long refundId;

    @Schema(example = "PENDING", accessMode = Schema.AccessMode.READ_ONLY)
    private String refundStatus;

    @Schema(example = "599.0", accessMode = Schema.AccessMode.READ_ONLY)
    private Double refundAmount;

    @NotNull(message = "User ID is required")
    @Schema(example = "1")
    private Long userId;

    @NotNull(message = "Bus ID is required")
    @Schema(example = "1")
    private Long busId;

    @Schema(example = "FastX Express", accessMode = Schema.AccessMode.READ_ONLY)
    private String busName;

    @Schema(example = "TN-01-AB-1234", accessMode = Schema.AccessMode.READ_ONLY)
    private String busNumber;

    @Schema(example = "AC_SLEEPER", accessMode = Schema.AccessMode.READ_ONLY)
    private String busType;

    @Schema(example = "Chennai", accessMode = Schema.AccessMode.READ_ONLY)
    private String origin;

    @Schema(example = "Bangalore", accessMode = Schema.AccessMode.READ_ONLY)
    private String destination;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate journeyDate;

    @Schema(example = "22:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalTime departureTime;

    @Schema(example = "06:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalTime arrivalTime;

    @Schema(example = "599.0", accessMode = Schema.AccessMode.READ_ONLY)
    private Double farePerSeat;

    @Valid
    @NotNull(message = "Passengers list is required")
    @Size(min = 1, message = "At least one passenger is required")
    @Schema
    private List<PassengerDto> passengers;
}
