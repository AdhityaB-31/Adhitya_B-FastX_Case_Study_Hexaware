package com.hexaware.fastx.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class PaymentDto {

    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long paymentId;

    @Schema(example = "599.0", accessMode = Schema.AccessMode.READ_ONLY)
    private Double amount;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(UPI|CREDIT_CARD|DEBIT_CARD|NET_BANKING)$",
             message = "Payment method must be UPI, CREDIT_CARD, DEBIT_CARD, or NET_BANKING")
    @Schema(example = "UPI",
            allowableValues = {"UPI", "CREDIT_CARD", "DEBIT_CARD", "NET_BANKING"})
    private String paymentMethod;

    @Schema(example = "TXN20260701001", accessMode = Schema.AccessMode.READ_ONLY)
    private String transactionId;

    @Schema(example = "PENDING",
            allowableValues = {"PENDING", "COMPLETED", "FAILED", "REFUNDED"}, accessMode = Schema.AccessMode.READ_ONLY)
    private String paymentStatus;

    @Schema(example = "2026-07-01T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime paymentDate;

    @Schema(example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long bookingId;
}
