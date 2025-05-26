package com.example.paymentbe.service.strategy;

import com.example.paymentbe.dto.PaymentRequest;

public interface PaymentStrategy {
    boolean process(PaymentRequest request);
}