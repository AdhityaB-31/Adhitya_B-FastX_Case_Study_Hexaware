package com.hexaware.fastx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class UserDto {

    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @NotBlank(message = "Full name is required")
    @Schema(example = "Adhitya")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(example = "adhitya@example.com")
    private String email;


    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
        message = "Password must be at least 8 characters and include an uppercase letter, a lowercase letter, a number, and a special character (@$!%*?&)"
    )
    @Schema(example = "Pass@1234")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    @Schema(example = "9876543210")
    private String phoneNumber;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER|Male|Female|Other)$", message = "Gender must be MALE, FEMALE, or OTHER")
    @Schema(example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
    private String gender;

    @NotBlank(message = "Address is required")
    @Schema(example = "123, Main Street, Chennai")
    private String address;

    @Schema(example = "true")
    private Boolean isActive;

    @Schema(example = "2")
    private Long roleId;
}
