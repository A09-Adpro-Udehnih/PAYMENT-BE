package com.example.paymentbe.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RefundStatusTest {

    @Test
    void testRefundStatusValues() {
        assertEquals(3, RefundStatus.values().length);
        assertArrayEquals(
            new RefundStatus[] {
                RefundStatus.PENDING,
                RefundStatus.ACCEPTED,
                RefundStatus.REJECTED
            },
            RefundStatus.values()
        );
    }

    @Test
    void testRefundStatusGetStatus() {
        assertEquals("PENDING", RefundStatus.PENDING.getStatus());
        assertEquals("ACCEPTED", RefundStatus.ACCEPTED.getStatus());
        assertEquals("REJECTED", RefundStatus.REJECTED.getStatus());
    }

    @Test
    void testRefundStatusValueOf() {
        assertSame(RefundStatus.PENDING, RefundStatus.valueOf("PENDING"));
        assertSame(RefundStatus.ACCEPTED, RefundStatus.valueOf("ACCEPTED"));
        assertSame(RefundStatus.REJECTED, RefundStatus.valueOf("REJECTED"));
    }
}