package com.example.paymentbe.service.strategy;

import com.example.paymentbe.enums.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentStrategyFactoryTest {

    @Mock
    private BankTransferStrategy bankTransferStrategy;

    @Mock
    private CreditCardStrategy creditCardStrategy;

    @InjectMocks
    private PaymentStrategyFactory strategyFactory;

    @Test
    void getStrategy_CreditCard() {
        PaymentStrategy strategy = strategyFactory.getStrategy(PaymentMethod.CREDIT_CARD);
        assertNotNull(strategy);
        assertTrue(strategy instanceof CreditCardStrategy);
    }

    @Test
    void getStrategy_BankTransfer() {
        PaymentStrategy strategy = strategyFactory.getStrategy(PaymentMethod.BANK_TRANSFER);
        assertNotNull(strategy);
        assertTrue(strategy instanceof BankTransferStrategy);
    }

    @Test
    void getStrategy_InvalidMethod() {
        assertThrows(IllegalArgumentException.class, () -> {
            strategyFactory.getStrategy(null);
        });
    }
}