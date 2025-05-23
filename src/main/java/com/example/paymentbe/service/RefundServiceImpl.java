package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundRequest;
import com.example.paymentbe.enums.PaymentStatus;
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
    public PaymentResponse requestRefund(String paymentId, RefundRequest request) {
        UUID paymentUUID = UUID.fromString(paymentId);
        Payment payment = paymentRepository.findById(paymentUUID)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Only PAID payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUND_REQUESTED);
        paymentRepository.save(payment);

        Refund refund = Refund.builder()
                .payment(payment)
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .build();
        refundRepository.save(refund);

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
                .refundReason(refund.getReason())
                .build();
    }

    @Override
    public List<PaymentResponse> getRefundRequests() {
        return paymentRepository.findByStatus(PaymentStatus.REFUND_REQUESTED).stream()
                .map(payment -> PaymentResponse.builder()
                        .paymentId(payment.getId().toString())
                        .userId(payment.getUserId())
                        .courseId(payment.getCourseId())
                        .amount(payment.getAmount())
                        .paymentMethod(payment.getMethod().name())
                        .status(payment.getStatus())
                        .paymentReference(payment.getPaymentReference())
                        .createdAt(payment.getCreatedAt())
                        .updatedAt(payment.getUpdatedAt())
                        .refundReason(payment.getRefund() != null ? payment.getRefund().getReason() : null)
                        .build())
                .collect(Collectors.toList());
    }
}
