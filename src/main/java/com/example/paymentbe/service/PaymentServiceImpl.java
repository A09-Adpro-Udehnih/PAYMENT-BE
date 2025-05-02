package com.example.paymentbe.service;

import com.example.paymentbe.dto.*;
import com.example.paymentbe.model.*;
import com.example.paymentbe.repository.PaymentRepository;
import com.example.paymentbe.service.strategy.PaymentStrategy;
import com.example.paymentbe.service.strategy.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentStrategyFactory strategyFactory;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        PaymentStrategy strategy = strategyFactory.getStrategy(request.getMethod());
        boolean paymentSuccess = strategy.processPayment(request.getAmount());

        PaymentStatus status = determinePaymentStatus(request.getMethod(), paymentSuccess);
        String paymentRef = generatePaymentReference();

        Payment payment = buildPaymentEntity(request, status, paymentRef);
        Payment savedPayment = paymentRepository.save(payment);

        return buildPaymentResponse(savedPayment, paymentSuccess, status);
    }

    // Helper methods
    private PaymentStatus determinePaymentStatus(PaymentMethod method, boolean success) {
        if (!success) return PaymentStatus.FAILED;
        return method == PaymentMethod.BANK_TRANSFER ? PaymentStatus.PENDING : PaymentStatus.PAID;
    }

    private String generatePaymentReference() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Payment buildPaymentEntity(PaymentRequest request, PaymentStatus status, String paymentRef) {
        return Payment.builder()
                .userId(request.getUserId())
                .courseId(request.getCourseId())
                .courseName(request.getCourseName())
                .tutorName(request.getTutorName())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(status)
                .transactionDate(LocalDateTime.now())
                .paymentReference(paymentRef)
                .build();
    }

    private PaymentResponse buildPaymentResponse(Payment payment, boolean success, PaymentStatus status) {
        String message = success ? 
            (status == PaymentStatus.PENDING ? 
                "Waiting for bank transfer confirmation" : "Payment successful") : 
            "Payment failed";
            
        return PaymentResponse.builder()
                .paymentId(payment.getId().toString())
                .status(payment.getStatus())
                .message(message)
                .paymentReference(payment.getPaymentReference())
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(String paymentId, String status) {
        Payment payment = paymentRepository.findById(UUID.fromString(paymentId))
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        PaymentStatus newStatus = PaymentStatus.valueOf(status.toUpperCase());
        payment.setStatus(newStatus);
        paymentRepository.save(payment);

        return PaymentResponse.builder()
                .paymentId(payment.getId().toString())
                .status(payment.getStatus())
                .message("Payment status updated to " + newStatus)
                .paymentReference(payment.getPaymentReference())
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse requestRefund(String paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(UUID.fromString(paymentId))
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Only PAID payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        return PaymentResponse.builder()
                .paymentId(payment.getId().toString())
                .status(payment.getStatus())
                .message("Refund requested: " + request.getReason())
                .paymentReference(payment.getPaymentReference())
                .build();
    }
}