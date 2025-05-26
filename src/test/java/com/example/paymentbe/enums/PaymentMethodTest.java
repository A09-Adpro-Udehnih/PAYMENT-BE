package com.example.paymentbe.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodTest {

    @Test
    void testPaymentMethodValues() {
        assertEquals(2, PaymentMethod.values().length);
        assertArrayEquals(
            new PaymentMethod[] {
                PaymentMethod.BANK_TRANSFER,
                PaymentMethod.CREDIT_CARD
            },
            PaymentMethod.values()
        );
    }

    @Test
    void testPaymentMethodValueOf() {
        assertSame(PaymentMethod.BANK_TRANSFER, PaymentMethod.valueOf("BANK_TRANSFER"));
        assertSame(PaymentMethod.CREDIT_CARD, PaymentMethod.valueOf("CREDIT_CARD"));
    }
}