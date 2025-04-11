package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.model.Payment;
import com.example.paymentbe.model.PaymentStatus;
import com.example.paymentbe.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        boolean success = simulatePaymentProcessing(request);

        PaymentStatus status = success ? PaymentStatus.PAID : PaymentStatus.FAILED;

        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(status)
                .paymentReference(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        return PaymentResponse.builder()
                .paymentId(saved.getId().toString())
                .status(saved.getStatus())
                .message(success ? "Payment successful" : "Payment failed")
                .build();
    }

    protected boolean simulatePaymentProcessing(PaymentRequest request) {
        return true;
    }
}
