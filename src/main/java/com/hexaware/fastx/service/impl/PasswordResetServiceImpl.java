package com.hexaware.fastx.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.entity.BusOperator;
import com.hexaware.fastx.entity.PasswordResetToken;
import com.hexaware.fastx.entity.User;
import com.hexaware.fastx.exception.InvalidTokenException;
import com.hexaware.fastx.repository.BusOperatorRepository;
import com.hexaware.fastx.repository.PasswordResetTokenRepository;
import com.hexaware.fastx.repository.UserRepository;
import com.hexaware.fastx.service.EmailService;
import com.hexaware.fastx.service.PasswordResetService;

@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final long TOKEN_VALIDITY_MINUTES = 30;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusOperatorRepository busOperatorRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String defaultFrontendUrl;

    @Override
    public void requestPasswordReset(String email, String frontendBaseUrl) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        Optional<BusOperator> operatorOpt = userOpt.isPresent() ? Optional.empty() : busOperatorRepository.findByEmail(email);

        // Deliberately do not throw / reveal whether the account exists.
        if (userOpt.isEmpty() && operatorOpt.isEmpty()) {
            logger.info("Password reset requested for unknown email: {}", email);
            return;
        }

        String accountType = userOpt.isPresent() ? "USER" : "OPERATOR";
        String recipientName = userOpt.map(User::getFullName)
                .orElseGet(() -> operatorOpt.map(BusOperator::getCompanyName).orElse(""));

        // Invalidate any previously issued, still-active tokens for this email.
        tokenRepository.invalidateActiveTokensForEmail(email);

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setTokenHash(tokenHash);
        resetToken.setEmail(email);
        resetToken.setAccountType(accountType);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        String base = (frontendBaseUrl != null && !frontendBaseUrl.isBlank()) ? frontendBaseUrl : defaultFrontendUrl;
        String resetLink = base.replaceAll("/$", "") + "/reset-password?token=" + rawToken;

        emailService.sendPasswordResetEmail(email, recipientName, resetLink);
    }

    @Override
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("This password reset link is invalid."));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("This password reset link has already been used.");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("This password reset link has expired. Please request a new one.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        if ("USER".equals(resetToken.getAccountType())) {
            User user = userRepository.findByEmail(resetToken.getEmail())
                    .orElseThrow(() -> new InvalidTokenException("Account associated with this link no longer exists."));
            user.setPassword(encodedPassword);
            userRepository.save(user);
        } else {
            BusOperator operator = busOperatorRepository.findByEmail(resetToken.getEmail())
                    .orElseThrow(() -> new InvalidTokenException("Account associated with this link no longer exists."));
            operator.setPassword(encodedPassword);
            busOperatorRepository.save(operator);
        }

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        logger.info("Password successfully reset for account: {}", resetToken.getEmail());
    }

    private String generateRawToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
