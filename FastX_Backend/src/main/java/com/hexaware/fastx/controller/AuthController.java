package com.hexaware.fastx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.fastx.exception.*;
import com.hexaware.fastx.dto.AuthRequest;
import com.hexaware.fastx.dto.BusOperatorDto;
import com.hexaware.fastx.dto.ForgotPasswordRequest;
import com.hexaware.fastx.dto.GoogleAuthRequest;
import com.hexaware.fastx.dto.GoogleUserInfo;
import com.hexaware.fastx.dto.ResetPasswordRequest;
import com.hexaware.fastx.dto.UserDto;
import com.hexaware.fastx.security.FastXUserDetails;
import com.hexaware.fastx.security.JwtService;
import com.hexaware.fastx.service.BusOperatorService;
import com.hexaware.fastx.service.GoogleAuthService;
import com.hexaware.fastx.service.PasswordResetService;
import com.hexaware.fastx.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private BusOperatorService busOperatorService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private GoogleAuthService googleAuthService;

    Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register/user")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserDto userDto) {
        UserDto savedUser = userService.createUser(userDto);
        logger.info("New user registered: {}", savedUser.getEmail());
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @PostMapping("/register/operator")
    public ResponseEntity<BusOperatorDto> registerOperator(@Valid @RequestBody BusOperatorDto operatorDto) {
        BusOperatorDto savedOperator = busOperatorService.createOperator(operatorDto);
        logger.info("New bus operator registered: {}", savedOperator.getEmail());
        return new ResponseEntity<>(savedOperator, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody AuthRequest authRequest) {

        // Check if the account is deactivated first
        try {
            UserDto user = userService.getUserByEmail(authRequest.getEmail());
            if (user != null && (user.getIsActive() == null || !user.getIsActive())) {
                return new ResponseEntity<>("Your account is deactivated. Please contact admin.", HttpStatus.FORBIDDEN);
            }
        } catch (ResourceNotFoundException e) {
            try {
                BusOperatorDto operator = busOperatorService.getOperatorByEmail(authRequest.getEmail());
                if (operator != null && (operator.getIsActive() == null || !operator.getIsActive())) {
                    return new ResponseEntity<>("Your account is deactivated. Please contact admin.",
                            HttpStatus.FORBIDDEN);
                }
            } catch (ResourceNotFoundException ex) {
                // Ignore, normal authentication will handle invalid email
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()));

            if (authentication.isAuthenticated()) {

                String role = authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("ROLE_USER");

                FastXUserDetails principal = (FastXUserDetails) authentication.getPrincipal();
                Long entityId = principal.getEntityId();

                String token = jwtService.generateToken(
                        authRequest.getEmail(),
                        role,
                        entityId,
                        principal.getDisplayName());

                logger.info("User logged in: {}", authRequest.getEmail());

                return ResponseEntity.ok(token);
            } else {
                throw new UsernameNotFoundException("Invalid email or password");
            }

        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for: {}", authRequest.getEmail());
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
            @RequestHeader(value = "Origin", required = false) String origin) {
        passwordResetService.requestPasswordReset(request.getEmail(), origin);
        logger.info("Password reset requested for: {}", request.getEmail());
        return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        logger.info("Password reset completed for a token");
        return ResponseEntity.ok("Your password has been reset successfully. You can now sign in.");
    }

    @PostMapping("/google")
    public ResponseEntity<String> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        GoogleUserInfo googleUserInfo = googleAuthService.verify(request.getIdToken());

        UserDto user = userService.findOrCreateGoogleUser(googleUserInfo);

        if (user != null && (user.getIsActive() == null || !user.getIsActive())) {
            return new ResponseEntity<>("Your account is deactivated. Please contact admin.", HttpStatus.FORBIDDEN);
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                "ROLE_USER",
                user.getUserId(),
                user.getFullName());

        logger.info("User signed in via Google: {}", user.getEmail());
        return ResponseEntity.ok(token);
    }
}
