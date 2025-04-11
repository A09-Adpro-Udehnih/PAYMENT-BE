package com.example.paymentbe.dto;

import com.example.paymentbe.model.PaymentMethod;
import lombok.Data;

@Data
public class PaymentRequest {
    private String userId;
    private double amount;
    private PaymentMethod method;
}
