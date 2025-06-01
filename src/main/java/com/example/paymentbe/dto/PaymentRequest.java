package com.example.paymentbe.dto;

import java.util.UUID;
import com.example.paymentbe.enums.PaymentMethod;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @Positive(message = "Amount must be positive")
    private double amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    private String bankAccount;
    private String cardNumber;
    private String cardCvc;
}