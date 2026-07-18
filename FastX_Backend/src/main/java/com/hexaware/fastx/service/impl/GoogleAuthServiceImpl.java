package com.hexaware.fastx.service.impl;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.hexaware.fastx.dto.GoogleUserInfo;
import com.hexaware.fastx.exception.UnauthorizedException;
import com.hexaware.fastx.service.GoogleAuthService;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthServiceImpl.class);

    @Value("${app.google.client-id:}")
    private String googleClientId;

    @Override
    public GoogleUserInfo verify(String idToken) {
        if (googleClientId == null || googleClientId.isBlank()) {
            logger.error("Google OAuth is not configured: app.google.client-id is missing");
            throw new UnauthorizedException("Google sign-in is not configured on the server.");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new UnauthorizedException("Invalid or expired Google sign-in token.");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();

            GoogleUserInfo info = new GoogleUserInfo();
            info.setGoogleId(payload.getSubject());
            info.setEmail(payload.getEmail());
            info.setEmailVerified(Boolean.TRUE.equals(payload.getEmailVerified()));
            Object name = payload.get("name");
            info.setFullName(name != null ? name.toString() : payload.getEmail());
            return info;
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to verify Google ID token: {}", e.getMessage());
            throw new UnauthorizedException("Could not verify Google sign-in. Please try again.");
        }
    }
}
