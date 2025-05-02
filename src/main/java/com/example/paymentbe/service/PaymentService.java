package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundRequest;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentResponse updatePaymentStatus(String paymentId, String status);
    PaymentResponse requestRefund(String paymentId, RefundRequest request);
}