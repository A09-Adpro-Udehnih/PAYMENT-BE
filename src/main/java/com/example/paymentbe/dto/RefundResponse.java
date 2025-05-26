package com.example.paymentbe.dto;

import com.example.paymentbe.enums.RefundStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RefundResponse {
    private UUID id;
    private UUID paymentId;
    private String reason;
    private String processedBy;
    private RefundStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private PaymentResponse payment;
} 