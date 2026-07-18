package com.hexaware.fastx.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload sent by the frontend after Google Identity Services returns
 * a signed credential (ID token) for the signed-in Google account.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthRequest {

    @NotBlank(message = "Google ID token is required")
    private String idToken;
}
