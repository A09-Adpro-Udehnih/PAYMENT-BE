package com.example.paymentbe.dto;

import com.example.paymentbe.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String paymentId;
    private PaymentStatus status;
    private String message;
    private String paymentReference;
}