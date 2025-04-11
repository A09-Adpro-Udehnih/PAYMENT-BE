package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
}
