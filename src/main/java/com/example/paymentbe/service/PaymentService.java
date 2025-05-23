package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentResponse getPayment(String paymentId);
    List<PaymentResponse> getUserPayments(String userId);
    PaymentResponse updatePaymentStatus(String paymentId, String status);
    List<PaymentResponse> getPendingPayments();
}