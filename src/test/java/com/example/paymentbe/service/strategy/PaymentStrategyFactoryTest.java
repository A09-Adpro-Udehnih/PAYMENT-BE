package com.example.paymentbe.service.strategy;

import com.example.paymentbe.enums.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentStrategyFactoryTest {

    @Mock
    private BankTransferStrategy bankTransferStrategy;

    @Mock
    private CreditCardStrategy creditCardStrategy;

    @InjectMocks
    private PaymentStrategyFactory strategyFactory;

    @Test
    void getStrategy_CreditCard_ReturnsCreditCardStrategy() {
        PaymentStrategy strategy = strategyFactory.getStrategy(PaymentMethod.CREDIT_CARD);
        assertNotNull(strategy);
        assertTrue(strategy instanceof CreditCardStrategy);
        assertSame(creditCardStrategy, strategy);
    }

    @Test
    void getStrategy_BankTransfer_ReturnsBankTransferStrategy() {
        PaymentStrategy strategy = strategyFactory.getStrategy(PaymentMethod.BANK_TRANSFER);
        assertNotNull(strategy);
        assertTrue(strategy instanceof BankTransferStrategy);
        assertSame(bankTransferStrategy, strategy);
    }

    @Test
    void getStrategy_NullMethod_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> strategyFactory.getStrategy(null)
        );
        assertEquals("Invalid payment method", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void getStrategy_AllPaymentMethods_ReturnsCorrectStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategyFactory.getStrategy(method);
        assertNotNull(strategy);
        
        switch (method) {
            case CREDIT_CARD:
                assertTrue(strategy instanceof CreditCardStrategy);
                break;
            case BANK_TRANSFER:
                assertTrue(strategy instanceof BankTransferStrategy);
                break;
            default:
                fail("Unexpected payment method: " + method);
        }
    }
}