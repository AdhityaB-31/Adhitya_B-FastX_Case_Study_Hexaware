package com.hexaware.fastx.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single-use, time-limited token issued when an account (User or BusOperator)
 * requests a password reset. The raw token is emailed to the account holder;
 * only its SHA-256 hash is stored here so a leaked database never exposes usable tokens.
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_reset_token_hash", columnList = "tokenHash", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String accountType;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
