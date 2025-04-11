package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.model.PaymentMethod;
import com.example.paymentbe.model.PaymentStatus;
import com.example.paymentbe.model.Payment;
import com.example.paymentbe.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    private PaymentRepository paymentRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        paymentService = new PaymentServiceImpl(paymentRepository); // RED, belum implementasi strategy
    }

    @Test
    void testProcessPaymentShouldReturnPaidStatusWhenSuccess() {
        PaymentRequest request = new PaymentRequest();
        request.setUserId("user1");
        request.setAmount(100.0);
        request.setMethod(PaymentMethod.BANK_TRANSFER);

        Payment savedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .userId("user1")
                .amount(100.0)
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.PAID)
                .paymentReference("ref123")
                .build();

        when(paymentRepository.save(any())).thenReturn(savedPayment);

        var response = paymentService.processPayment(request);

        assertEquals(PaymentStatus.PAID, response.getStatus());
        assertEquals("Payment successful", response.getMessage());
    }

    @Test
    void testProcessPaymentShouldReturnFailedStatusWhenFailed() {
        PaymentRequest request = new PaymentRequest();
        request.setUserId("user2");
        request.setAmount(100.0);
        request.setMethod(PaymentMethod.CREDIT_CARD);

        // override service untuk simulate false
        PaymentService failingService = new PaymentServiceImpl(paymentRepository) {
            @Override
            protected boolean simulatePaymentProcessing(PaymentRequest request) {
                return false;
            }
        };

        Payment failedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .userId("user2")
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.FAILED)
                .paymentReference("ref456")
                .build();

        when(paymentRepository.save(any())).thenReturn(failedPayment);

        var response = failingService.processPayment(request);

        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertEquals("Payment failed", response.getMessage());
    }
}
