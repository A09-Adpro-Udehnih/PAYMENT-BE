package com.example.paymentbe.service;

import com.example.paymentbe.dto.RefundRequest;
import com.example.paymentbe.dto.RefundResponse;
import java.util.List;

public interface RefundService {
    RefundResponse requestRefund(String paymentId, RefundRequest request);
    RefundResponse processRefund(String refundId, String status, String processedBy);
    List<RefundResponse> getPendingRefunds();
    RefundResponse getRefund(String refundId);
}