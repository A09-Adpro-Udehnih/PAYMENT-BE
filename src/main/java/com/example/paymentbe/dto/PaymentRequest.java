package com.example.paymentbe.dto;

import com.example.paymentbe.model.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Course ID is required")
    private String courseId;

    @Positive(message = "Amount must be positive")
    private double amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    private String bankAccount; // Required for BANK_TRANSFER
    private String cardNumber;  // Required for CREDIT_CARD
    private String cardCvc;     // Required for CREDIT_CARD
}