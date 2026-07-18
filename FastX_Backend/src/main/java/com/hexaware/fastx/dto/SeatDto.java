package com.hexaware.fastx.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class SeatDto {
    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long seatId;

    @NotBlank(message = "Seat number is required")
    @Schema(example = "B1-LB-01")
    private String seatNumber;

    @NotBlank(message = "Seat type is required")
    @Schema(example = "LOWER_BERTH",
            allowableValues = {"LOWER_BERTH", "UPPER_BERTH", "SIDE_LOWER", "SIDE_UPPER",
                    "WINDOW_SEAT", "AISLE_SEAT", "MIDDLE_SEAT"})
    private String seatType;

    @Schema(example = "AVAILABLE",
            allowableValues = {"AVAILABLE", "RESERVED", "BOOKED"})
    private String seatStatus;

    @NotNull(message = "Bus ID is required")
    @Schema(example = "1")
    private Long busId;

    @Schema(example = "1")
    private Long reservedByBookingId;
}
