package com.example.paymentbe.dto;

import com.example.paymentbe.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private String paymentId;
    private String userId;
    private String courseId;
    private double amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String paymentReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String refundReason; // Only for refunded payments
}