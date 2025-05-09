package com.example.paymentbe.service;

import com.example.paymentbe.dto.*;
import com.example.paymentbe.model.*;
import com.example.paymentbe.repository.PaymentRepository;
import com.example.paymentbe.repository.RefundRepository;
import com.example.paymentbe.service.strategy.*;
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
    private final RefundRepository refundRepository;
    private final PaymentStrategyFactory strategyFactory;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request cannot be null");
        }
        validatePaymentRequest(request);

        PaymentStrategy strategy = strategyFactory.getStrategy(request.getMethod());
        boolean isSuccess = strategy.process(request);

        Payment payment = buildPayment(request, isSuccess);
        payment = paymentRepository.save(payment);
        
        return buildPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        if (paymentId == null || paymentId.isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be null or empty");
        }
        
        Payment payment = paymentRepository.findById(UUID.fromString(paymentId))
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
        return buildPaymentResponse(payment);
    }

    @Override
    public List<PaymentResponse> getUserPayments(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        
        return paymentRepository.findByUserId(userId).stream()
                .map(this::buildPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponse requestRefund(String paymentId, RefundRequest request) {
        if (paymentId == null || paymentId.isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be null or empty");
        }
        if (request == null) {
            throw new IllegalArgumentException("Refund request cannot be null");
        }

        UUID paymentUUID = UUID.fromString(paymentId);
        Payment payment = paymentRepository.findById(paymentUUID)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Only PAID payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUND_REQUESTED);
        paymentRepository.save(payment);
        
        Refund refund = Refund.builder()
                .payment(payment)  // Diubah dari paymentId ke payment
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .build();
        refundRepository.save(refund);
        
        return buildPaymentResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPendingPayments() {
        return paymentRepository.findByStatus(PaymentStatus.PENDING).stream()
                .map(this::buildPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getRefundRequests() {
        return paymentRepository.findByStatus(PaymentStatus.REFUND_REQUESTED).stream()
                .map(this::buildPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(String paymentId, String status) {
        if (paymentId == null || paymentId.isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be null or empty");
        }
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        Payment payment = paymentRepository.findById(UUID.fromString(paymentId))
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        try {
            PaymentStatus newStatus = PaymentStatus.valueOf(status.toUpperCase());
            payment.setStatus(newStatus);
            paymentRepository.save(payment);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status: " + status);
        }
        
        return buildPaymentResponse(payment);
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (request.getMethod() == PaymentMethod.BANK_TRANSFER && 
            (request.getBankAccount() == null || request.getBankAccount().isBlank())) {
            throw new IllegalArgumentException("Bank account is required for bank transfer");
        }
        if (request.getMethod() == PaymentMethod.CREDIT_CARD && 
            (request.getCardNumber() == null || request.getCardNumber().isBlank())) {
            throw new IllegalArgumentException("Card number is required for credit card payment");
        }
    }

    private Payment buildPayment(PaymentRequest request, boolean isSuccess) {
        String paymentRef = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String cardLastFour = null;
        if (request.getMethod() == PaymentMethod.CREDIT_CARD && request.getCardNumber() != null) {
            cardLastFour = request.getCardNumber().substring(Math.max(0, request.getCardNumber().length() - 4));
        }

        PaymentStatus status = determinePaymentStatus(request.getMethod(), isSuccess);

        return Payment.builder()
                .userId(request.getUserId())
                .courseId(request.getCourseId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(status)
                .bankAccount(request.getBankAccount())
                .cardLastFour(cardLastFour)
                .paymentReference(paymentRef)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private PaymentStatus determinePaymentStatus(PaymentMethod method, boolean success) {
        if (!success) return PaymentStatus.FAILED;
        return method == PaymentMethod.BANK_TRANSFER ? PaymentStatus.PENDING : PaymentStatus.PAID;
    }

    private PaymentResponse buildPaymentResponse(Payment payment) {
        PaymentResponse.PaymentResponseBuilder builder = PaymentResponse.builder()
                .paymentId(payment.getId().toString())
                .userId(payment.getUserId())
                .courseId(payment.getCourseId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getMethod().name())
                .status(payment.getStatus())
                .paymentReference(payment.getPaymentReference())
                .createdAt(payment.getCreatedAt());
        
        if (payment.getUpdatedAt() != null) {
            builder.updatedAt(payment.getUpdatedAt());
        }
        
        // Tambahkan informasi refund jika ada
        if (payment.getRefund() != null) {
            builder.refundReason(payment.getRefund().getReason());
        }
        
        return builder.build();
    }
}