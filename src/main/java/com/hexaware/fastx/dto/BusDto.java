package com.hexaware.fastx.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
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
public class BusDto {

    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long busId;

    @NotBlank(message = "Bus name is required")
    @Schema(example = "FastX Express")
    private String busName;

    @NotBlank(message = "Bus number is required")
    @Schema(example = "TN-01-AB-1234")
    private String busNumber;

    @NotBlank(message = "Bus type is required")
    @Pattern(regexp = "^(AC_SLEEPER|NON_AC_SLEEPER|AC_SEATER|NON_AC_SEATER|VOLVO)$", message = "Bus type must be AC_SLEEPER, NON_AC_SLEEPER, AC_SEATER, NON_AC_SEATER, or VOLVO")
    @Schema(example = "AC_SLEEPER", allowableValues = { "AC_SLEEPER", "NON_AC_SLEEPER", "AC_SEATER", "NON_AC_SEATER",
            "VOLVO" })
    private String busType;

    @NotBlank(message = "Origin is required")
    @Schema(example = "Chennai")
    private String origin;

    @NotBlank(message = "Destination is required")
    @Schema(example = "Bangalore")
    private String destination;

    @NotNull(message = "Journey date is required")
    @FutureOrPresent(message = "Journey date must be today or a future date")
    @Schema(example = "2026-07-01")
    private LocalDate journeyDate;

    @NotNull(message = "Departure time is required")
    @Schema(example = "22:00:00")
    private LocalTime departureTime;

    @NotNull(message = "Arrival time is required")
    @Schema(example = "06:00:00")
    private LocalTime arrivalTime;

    @NotNull(message = "Fare is required")
    @Min(value = 1, message = "Fare must be greater than 0")
    @Schema(example = "599.0")
    private Double fare;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    @Schema(example = "36")
    private Integer totalSeats;

    @NotNull(message = "Operator ID is required")
    @Schema(example = "1")
    private Long operatorId;

    @Schema(example = "[1, 2, 3]")
    private List<Long> amenityIds;

    @Schema(example = "[\"WiFi\", \"Charging Point\"]", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> amenityNames;
}
