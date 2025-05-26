package com.example.paymentbe.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentStatusTest {

    @Test
    void testPaymentStatusValues() {
        assertEquals(5, PaymentStatus.values().length);
        assertArrayEquals(
            new PaymentStatus[] {
                PaymentStatus.PENDING,
                PaymentStatus.PAID,
                PaymentStatus.FAILED,
                PaymentStatus.REFUND_REQUESTED,
                PaymentStatus.REFUNDED
            },
            PaymentStatus.values()
        );
    }

    @Test
    void testPaymentStatusValueOf() {
        assertSame(PaymentStatus.PENDING, PaymentStatus.valueOf("PENDING"));
        assertSame(PaymentStatus.PAID, PaymentStatus.valueOf("PAID"));
        assertSame(PaymentStatus.FAILED, PaymentStatus.valueOf("FAILED"));
        assertSame(PaymentStatus.REFUND_REQUESTED, PaymentStatus.valueOf("REFUND_REQUESTED"));
        assertSame(PaymentStatus.REFUNDED, PaymentStatus.valueOf("REFUNDED"));
    }
}