package com.example.paymentbe.model;

import com.example.paymentbe.enums.RefundStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RefundTest {

    @Test
    public void testRefundCreation() {
        UUID refundId = UUID.randomUUID();
        Payment payment = new Payment();
        String reason = "Customer requested refund";
        String processedBy = "admin";
        LocalDateTime now = LocalDateTime.now();

        Refund refund = Refund.builder()
                .id(refundId)
                .payment(payment)
                .reason(reason)
                .processedBy(processedBy)
                .status(RefundStatus.PENDING)
                .createdAt(now)
                .requestedAt(now)
                .build();

        assertEquals(refundId, refund.getId());
        assertEquals(payment, refund.getPayment());
        assertEquals(reason, refund.getReason());
        assertEquals(processedBy, refund.getProcessedBy());
        assertEquals(RefundStatus.PENDING, refund.getStatus());
        assertEquals(now, refund.getCreatedAt());
        assertEquals(now, refund.getRequestedAt());
    }

    @Test
    public void testRefundNoArgsConstructor() {
        Refund refund = new Refund();

        assertNull(refund.getId());
        assertNull(refund.getPayment());
        assertNull(refund.getReason());
        assertNull(refund.getProcessedBy());
        assertNull(refund.getStatus());
        assertNull(refund.getCreatedAt());
        assertNull(refund.getRequestedAt());
    }

    @Test
    public void testRefundAllArgsConstructor() {
        UUID refundId = UUID.randomUUID();
        Payment payment = new Payment();
        String reason = "Product damaged";
        String processedBy = "manager";
        LocalDateTime now = LocalDateTime.now();

        Refund refund = new Refund(refundId, payment, reason, processedBy, RefundStatus.PENDING, now, now, null);

        assertEquals(refundId, refund.getId());
        assertEquals(payment, refund.getPayment());
        assertEquals(reason, refund.getReason());
        assertEquals(processedBy, refund.getProcessedBy());
        assertEquals(RefundStatus.PENDING, refund.getStatus());
        assertEquals(now, refund.getCreatedAt());
        assertEquals(now, refund.getRequestedAt());
    }

    @Test
    public void testRefundSettersAndGetters() {
        Refund refund = new Refund();
        UUID refundId = UUID.randomUUID();
        Payment payment = new Payment();
        String reason = "Wrong product delivered";
        String processedBy = "support";
        LocalDateTime now = LocalDateTime.now();

        refund.setId(refundId);
        refund.setPayment(payment);
        refund.setReason(reason);
        refund.setProcessedBy(processedBy);
        refund.setStatus(RefundStatus.PENDING);
        refund.setCreatedAt(now);
        refund.setRequestedAt(now);

        assertEquals(refundId, refund.getId());
        assertEquals(payment, refund.getPayment());
        assertEquals(reason, refund.getReason());
        assertEquals(processedBy, refund.getProcessedBy());
        assertEquals(RefundStatus.PENDING, refund.getStatus());
        assertEquals(now, refund.getCreatedAt());
        assertEquals(now, refund.getRequestedAt());
    }

    @Test
    public void testRefundEqualsAndHashCode() {
        UUID sharedId = UUID.randomUUID();
        Payment payment = new Payment();
        LocalDateTime now = LocalDateTime.now();
        
        Refund refund1 = Refund.builder()
                .id(sharedId)
                .payment(payment)
                .reason("Reason 1")
                .processedBy("admin1")
                .status(RefundStatus.PENDING)
                .createdAt(now)
                .requestedAt(now)
                .build();
                
        Refund refund2 = Refund.builder()
                .id(sharedId)
                .payment(payment)
                .reason("Reason 1")
                .processedBy("admin1")
                .status(RefundStatus.PENDING)
                .createdAt(now)
                .requestedAt(now)
                .build();
                
        Refund refund3 = Refund.builder()
                .id(UUID.randomUUID())
                .payment(payment)
                .reason("Reason 2")
                .processedBy("admin2")
                .status(RefundStatus.ACCEPTED)
                .createdAt(now)
                .requestedAt(now)
                .build();

        assertEquals(refund1, refund2);
        assertEquals(refund1.hashCode(), refund2.hashCode());
        assertNotEquals(refund1, refund3);
        assertNotEquals(refund1.hashCode(), refund3.hashCode());
    }

    @Test
    public void testRefundToString() {
        UUID refundId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        String reason = "Test reason";
        String processedBy = "test-admin";
        LocalDateTime now = LocalDateTime.now();

        Refund refund = Refund.builder()
                .id(refundId)
                .payment(payment)
                .reason(reason)
                .processedBy(processedBy)
                .status(RefundStatus.PENDING)
                .createdAt(now)
                .requestedAt(now)
                .build();

        String toStringResult = refund.toString();

        assertTrue(toStringResult.contains(refundId.toString()));
        assertTrue(toStringResult.contains(payment.toString()));
        assertTrue(toStringResult.contains(reason));
        assertTrue(toStringResult.contains(processedBy));
        assertTrue(toStringResult.contains(RefundStatus.PENDING.toString()));
        assertTrue(toStringResult.contains(now.toString()));
    }

    @Test
    public void testPaymentRelationship() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(paymentId);
        LocalDateTime now = LocalDateTime.now();

        Refund refund = Refund.builder()
                .payment(payment)
                .reason("Test payment relationship")
                .processedBy("system")
                .status(RefundStatus.PENDING)
                .createdAt(now)
                .requestedAt(now)
                .build();

        assertNotNull(refund.getPayment(), "Payment relationship should be established");
        assertEquals(paymentId, refund.getPayment().getId(), "Payment ID should match");
    }
}