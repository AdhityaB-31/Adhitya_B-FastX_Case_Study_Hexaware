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
public class RoleDto {
    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long roleId;

    @NotBlank(message = "Role name is required")
    @Schema(example = "ADMIN",
            allowableValues = {"ADMIN", "USER"})
    private String roleName;
}
