package com.example.paymentbe.model;

import com.example.paymentbe.enums.*;

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
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Payment payment = Payment.builder()
                .id(id)
                .userId(userId)
                .courseId(courseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .bankAccount("1234567890")
                .cardLastFour("1234")
                .paymentReference("PAY-12345")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(id, payment.getId());
        assertEquals(userId, payment.getUserId());
        assertEquals(courseId, payment.getCourseId());
        assertEquals(100.0, payment.getAmount());
        assertEquals(PaymentMethod.CREDIT_CARD, payment.getMethod());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals("1234567890", payment.getBankAccount());
        assertEquals("1234", payment.getCardLastFour());
        assertEquals("PAY-12345", payment.getPaymentReference());
        assertEquals(now, payment.getCreatedAt());
        assertEquals(now, payment.getUpdatedAt());
    }

    @Test
    void testPaymentSettersAndGetters() {
        Payment payment = new Payment();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        payment.setId(id);
        payment.setUserId(userId);
        payment.setCourseId(courseId);
        payment.setAmount(100.0);
        payment.setMethod(PaymentMethod.BANK_TRANSFER);
        payment.setStatus(PaymentStatus.PAID);
        payment.setBankAccount("1234567890");
        payment.setCardLastFour("1234");
        payment.setPaymentReference("PAY-12345");
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        assertEquals(id, payment.getId());
        assertEquals(userId, payment.getUserId());
        assertEquals(courseId, payment.getCourseId());
        assertEquals(100.0, payment.getAmount());
        assertEquals(PaymentMethod.BANK_TRANSFER, payment.getMethod());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals("1234567890", payment.getBankAccount());
        assertEquals("1234", payment.getCardLastFour());
        assertEquals("PAY-12345", payment.getPaymentReference());
        assertEquals(now, payment.getCreatedAt());
        assertEquals(now, payment.getUpdatedAt());
    }

    @Test
    void testPaymentEqualsAndHashCode() {
        UUID sharedId = UUID.randomUUID();
        UUID sharedUserId = UUID.randomUUID();
        UUID sharedCourseId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Payment payment1 = Payment.builder()
                .id(sharedId)
                .userId(sharedUserId)
                .courseId(sharedCourseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .bankAccount("1234567890")
                .cardLastFour("1234")
                .paymentReference("PAY-12345")
                .createdAt(now)
                .updatedAt(now)
                .build();

        Payment payment2 = Payment.builder()
                .id(sharedId)
                .userId(sharedUserId)
                .courseId(sharedCourseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .bankAccount("1234567890")
                .cardLastFour("1234")
                .paymentReference("PAY-12345")
                .createdAt(now)
                .updatedAt(now)
                .build();

        Payment payment3 = Payment.builder()
                .id(UUID.randomUUID())  // Different ID
                .userId(sharedUserId)
                .courseId(sharedCourseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .bankAccount("1234567890")
                .cardLastFour("1234")
                .paymentReference("PAY-12345")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Test equals
        assertEquals(payment1, payment2);
        assertNotEquals(payment1, payment3);
        assertNotEquals(payment1, null);
        assertNotEquals(payment1, "not a payment");

        // Test hashCode
        assertEquals(payment1.hashCode(), payment2.hashCode());
        assertNotEquals(payment1.hashCode(), payment3.hashCode());
    }

    @Test
    void testAddRefund() {
        Payment payment = new Payment();
        Refund refund = new Refund();

        payment.addRefund(refund);

        assertNotNull(payment.getRefund());
        assertEquals(payment, refund.getPayment());
        assertEquals(refund, payment.getRefund());
    }

    @Test
    void testToString() {
        UUID id = UUID.randomUUID();
        Payment payment = Payment.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .courseId(UUID.randomUUID())
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .build();

        String toString = payment.toString();
        
        assertTrue(toString.contains(id.toString()));
        assertTrue(toString.contains("CREDIT_CARD"));
        assertTrue(toString.contains("PAID"));
        assertTrue(toString.contains("100.0"));
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