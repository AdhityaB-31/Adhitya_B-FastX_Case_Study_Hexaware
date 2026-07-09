package com.hexaware.fastx.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema
public class AmenityDto {

    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long amenityId;

    @NotBlank(message = "Amenity name is required")
    @Schema(example = "WiFi")
    private String amenityName;
}
