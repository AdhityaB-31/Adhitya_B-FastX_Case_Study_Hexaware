package com.hexaware.fastx.service;

public interface PasswordResetService {

    /**
     * Kicks off the forgot-password flow for the given email. Always completes
     * silently (no exception, no indication of whether the account exists) to
     * avoid leaking which emails are registered.
     */
    void requestPasswordReset(String email, String frontendBaseUrl);

    /**
     * Validates the raw token and updates the matching account's password.
     * Throws an exception with a user-facing message if the token is invalid,
     * expired, or already used.
     */
    void resetPassword(String rawToken, String newPassword);
}
