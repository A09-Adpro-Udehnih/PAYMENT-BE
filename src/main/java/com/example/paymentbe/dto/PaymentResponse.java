package com.example.paymentbe.dto;

import com.example.paymentbe.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private String paymentId;
    private UUID userId;
    private UUID courseId;
    private double amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String paymentReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String refundReason; // Only for refunded payments
}