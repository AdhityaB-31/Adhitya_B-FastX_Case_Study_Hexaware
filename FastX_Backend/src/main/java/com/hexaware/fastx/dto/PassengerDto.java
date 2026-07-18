package com.hexaware.fastx.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema
public class PassengerDto {

    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long passengerId;

    @NotBlank(message = "Passenger name is required")
    @Schema(example = "Adhitya")
    private String name;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must be a valid value (max 120)")
    @Schema(example = "25")
    private Integer age;

    @NotNull(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    @Schema(example = "FEMALE",
            allowableValues = {"MALE", "FEMALE", "OTHER"})
    private String gender;

    @NotNull(message = "Seat ID is required")
    @Schema(example = "1")
    private Long seatId;

    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long bookingId;

    /** Seat number like B1-LB-01 — populated server-side when reading a booking. */
    @Schema(example = "B1-LB-01", accessMode = Schema.AccessMode.READ_ONLY)
    private String seatNumber;

    @Schema(example = "LOWER_BERTH", accessMode = Schema.AccessMode.READ_ONLY)
    private String seatType;
}
