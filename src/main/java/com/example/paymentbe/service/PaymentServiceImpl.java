package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.enums.PaymentMethod;
import com.example.paymentbe.enums.PaymentStatus;
import com.example.paymentbe.model.Payment;
import com.example.paymentbe.repository.PaymentRepository;
import com.example.paymentbe.service.strategy.PaymentStrategy;
import com.example.paymentbe.service.strategy.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentStrategyFactory strategyFactory;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        validatePaymentRequest(request);
        PaymentStrategy strategy = strategyFactory.getStrategy(request.getMethod());
        boolean isSuccess = strategy.process(request);
        Payment payment = buildPayment(request, isSuccess);
        payment = paymentRepository.save(payment);
        return buildPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        UUID paymentUUID = UUID.fromString(paymentId);
        Payment payment = paymentRepository.findById(paymentUUID)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
        return buildPaymentResponse(payment);
    }

    @Override
    public List<PaymentResponse> getUserPayments(String userId) {
        UUID userUUID = UUID.fromString(userId);
        return paymentRepository.findByUserId(userUUID).stream()
                .map(this::buildPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPendingPayments() {
        return paymentRepository.findByStatus(PaymentStatus.PENDING).stream()
                .map(this::buildPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(String paymentId, String status) {
        UUID paymentUUID = UUID.fromString(paymentId);
        Payment payment = paymentRepository.findById(paymentUUID)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
        try {
            PaymentStatus newStatus = PaymentStatus.valueOf(status.toUpperCase());
            payment.setStatus(newStatus);
            paymentRepository.save(payment);
            return buildPaymentResponse(payment);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status: " + status);
        }
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getUserId() == null || request.getCourseId() == null ||
            request.getMethod() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid payment request");
        }
    }

    private Payment buildPayment(PaymentRequest request, boolean isSuccess) {
        String ref = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String lastFour = null;
        if (request.getMethod() == PaymentMethod.CREDIT_CARD && request.getCardNumber() != null) {
            lastFour = request.getCardNumber().substring(Math.max(0, request.getCardNumber().length() - 4));
        }
        PaymentStatus status = !isSuccess ? PaymentStatus.FAILED :
            request.getMethod() == PaymentMethod.BANK_TRANSFER ? PaymentStatus.PENDING : PaymentStatus.PAID;

        return Payment.builder()
                .userId(request.getUserId())
                .courseId(request.getCourseId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(status)
                .bankAccount(request.getBankAccount())
                .cardLastFour(lastFour)
                .paymentReference(ref)
                .createdAt(LocalDateTime.now())
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
                .refundReason(payment.getRefund() != null ? payment.getRefund().getReason() : null)
                .build();
    }
}