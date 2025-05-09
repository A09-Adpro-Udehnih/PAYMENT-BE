package com.example.paymentbe.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {
    
    @Test
    void testPaymentBuilder() {
        LocalDateTime now = LocalDateTime.now();
        UUID paymentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        
        Payment payment = Payment.builder()
                .id(paymentId)
                .userId(userId)
                .courseId(courseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .paymentReference("PAY-123456")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertAll(
            () -> assertEquals(paymentId, payment.getId()),
            () -> assertEquals(userId, payment.getUserId()),
            () -> assertEquals(courseId, payment.getCourseId()),
            () -> assertEquals(100.0, payment.getAmount()),
            () -> assertEquals(PaymentMethod.CREDIT_CARD, payment.getMethod()),
            () -> assertEquals(PaymentStatus.PAID, payment.getStatus()),
            () -> assertEquals("PAY-123456", payment.getPaymentReference()),
            () -> assertEquals(now, payment.getCreatedAt()),
            () -> assertEquals(now, payment.getUpdatedAt())
        );
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void testPaymentMethod(PaymentMethod method) {
        Payment payment = Payment.builder()
                .method(method)
                .build();
        
        assertEquals(method, payment.getMethod());
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    void testPaymentStatus(PaymentStatus status) {
        Payment payment = Payment.builder()
                .status(status)
                .build();
        
        assertEquals(status, payment.getStatus());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 100.0, 999999.99})
    void testPaymentAmount(double amount) {
        Payment payment = Payment.builder()
                .amount(amount)
                .build();
        
        assertEquals(amount, payment.getAmount());
    }

    @Test
    void testPaymentWithRefund() {
        Payment payment = Payment.builder().build();
        Refund refund = Refund.builder()
                .payment(payment)
                .build();
        
        payment.setRefund(refund);
        
        assertNotNull(payment.getRefund());
        assertEquals(payment, refund.getPayment());
    }
}