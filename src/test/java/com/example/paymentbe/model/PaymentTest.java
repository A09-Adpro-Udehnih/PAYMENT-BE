package com.example.paymentbe.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {
    @Test
    void testCreatePaymentEntity() {
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .userId("user123")
                .amount(100.0)
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.PENDING)
                .paymentReference("ref123")
                .createdAt(LocalDateTime.now())
                .build();

        assertEquals("user123", payment.getUserId());
        assertEquals(PaymentMethod.BANK_TRANSFER, payment.getMethod());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }
}
