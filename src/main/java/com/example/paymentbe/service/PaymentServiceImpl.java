package com.example.paymentbe.service.impl;

import com.example.paymentbe.dto.*;
import com.example.paymentbe.model.*;
import com.example.paymentbe.repository.PaymentRepository;
import com.example.paymentbe.service.PaymentService;
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
        boolean isSuccess = strategy.processPayment(request.getAmount());

        PaymentStatus status = isSuccess ? 
            (request.getMethod() == PaymentMethod.BANK_TRANSFER ? 
                PaymentStatus.PENDING : PaymentStatus.PAID) : 
            PaymentStatus.FAILED;

        String paymentRef = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
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

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.builder()
                .paymentId(savedPayment.getId().toString())
                .status(savedPayment.getStatus())
                .message(isSuccess ? 
                    (status == PaymentStatus.PENDING ? 
                        "Waiting for bank transfer confirmation" : "Payment successful") : 
                    "Payment failed")
                .paymentReference(savedPayment.getPaymentReference())
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(String paymentId, String status) {
        Payment payment = paymentRepository.findById(UUID.fromString(paymentId))
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new PaymentException("Only PAID payments can be refunded");
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