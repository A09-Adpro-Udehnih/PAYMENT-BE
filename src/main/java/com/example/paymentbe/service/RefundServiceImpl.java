package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundRequest;
import com.example.paymentbe.dto.RefundResponse;
import com.example.paymentbe.enums.PaymentStatus;
import com.example.paymentbe.enums.RefundStatus;
import com.example.paymentbe.model.Payment;
import com.example.paymentbe.model.Refund;
import com.example.paymentbe.repository.PaymentRepository;
import com.example.paymentbe.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    @Override
    @Transactional
    public RefundResponse requestRefund(String paymentId, RefundRequest request) {
        UUID paymentUUID = UUID.fromString(paymentId);
        Payment payment = paymentRepository.findById(paymentUUID)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Only PAID payments can be refunded");
        }

        if (refundRepository.existsByPaymentId(paymentUUID)) {
            throw new IllegalStateException("Refund request already exists for this payment");
        }

        payment.setStatus(PaymentStatus.REFUND_REQUESTED);
        paymentRepository.save(payment);

        Refund refund = Refund.builder()
                .payment(payment)
                .reason(request.getReason())
                .status(RefundStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .requestedAt(LocalDateTime.now())
                .build();
        
        refund = refundRepository.save(refund);
        return buildRefundResponse(refund);
    }

    @Override
    @Transactional
    public RefundResponse processRefund(String refundId, String status, String processedBy) {
        UUID refundUUID = UUID.fromString(refundId);
        Refund refund = refundRepository.findById(refundUUID)
                .orElseThrow(() -> new RuntimeException("Refund not found with ID: " + refundId));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new IllegalStateException("Only PENDING refunds can be processed");
        }

        RefundStatus newStatus = RefundStatus.valueOf(status.toUpperCase());
        refund.setProcessedAt(LocalDateTime.now());
        refund.setProcessedBy(processedBy);
        refund.setStatus(newStatus);

        Payment payment = refund.getPayment();
        payment.setStatus(newStatus == RefundStatus.ACCEPTED ? 
                PaymentStatus.REFUNDED : 
                (newStatus == RefundStatus.REJECTED ? PaymentStatus.PAID : PaymentStatus.REFUND_REQUESTED));
        
        paymentRepository.save(payment);
        refund = refundRepository.save(refund);
        
        return buildRefundResponse(refund);
    }

    @Override
    public List<RefundResponse> getPendingRefunds() {
        return refundRepository.findByStatus(RefundStatus.PENDING).stream()
                .map(this::buildRefundResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RefundResponse getRefund(String refundId) {
        UUID refundUUID = UUID.fromString(refundId);
        Refund refund = refundRepository.findById(refundUUID)
                .orElseThrow(() -> new RuntimeException("Refund not found with ID: " + refundId));
        return buildRefundResponse(refund);
    }

    private RefundResponse buildRefundResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .paymentId(refund.getPayment().getId())
                .reason(refund.getReason())
                .processedBy(refund.getProcessedBy())
                .status(refund.getStatus())
                .createdAt(refund.getCreatedAt())
                .requestedAt(refund.getRequestedAt())
                .processedAt(refund.getProcessedAt())
                .payment(buildPaymentResponse(refund.getPayment()))
                .build();
    }

    private PaymentResponse buildPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId().toString())
                .userId(payment.getUserId())
                .courseId(payment.getCourseId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getMethod().name())
                .status(payment.getStatus())
                .paymentReference(payment.getPaymentReference())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
