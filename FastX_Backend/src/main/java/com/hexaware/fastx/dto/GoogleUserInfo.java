package com.hexaware.fastx.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Verified profile fields extracted from a Google ID token. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfo {
    private String googleId;
    private String email;
    private boolean emailVerified;
    private String fullName;
}
