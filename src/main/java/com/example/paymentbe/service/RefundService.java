package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundRequest;

import java.util.List;

public interface RefundService {
    PaymentResponse requestRefund(String paymentId, RefundRequest request);
    List<PaymentResponse> getRefundRequests();
}