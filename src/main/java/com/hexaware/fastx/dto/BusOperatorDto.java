package com.hexaware.fastx.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class BusOperatorDto {

    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long operatorId;

    @NotBlank(message = "Company name is required")
    @Schema(example = "FastX Travels Pvt Ltd")
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(example = "info@fastxtravels.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$", message = "Password must be at least 8 characters and include an uppercase letter, a lowercase letter, a number, and a special character (@$!%*?&)")
    @Schema(example = "Operator@1234")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    @Schema(example = "9876543210")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Schema(example = "456, Transport Nagar, Chennai")
    private String address;

    @Schema(example = "true")
    private Boolean isActive;
}
