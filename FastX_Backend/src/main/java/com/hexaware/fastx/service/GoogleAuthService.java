package com.hexaware.fastx.service;

import com.hexaware.fastx.dto.GoogleUserInfo;

public interface GoogleAuthService {

    /**
     * Verifies a Google Identity Services ID token against Google's public keys
     * and the configured OAuth client ID, and extracts the verified profile.
     * Throws if the token is missing, expired, malformed, or was issued for a
     * different client ID.
     */
    GoogleUserInfo verify(String idToken);
}
