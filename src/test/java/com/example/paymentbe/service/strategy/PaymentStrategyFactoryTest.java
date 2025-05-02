package com.example.paymentbe.service.strategy; // Ubah package ke service.strategy

import com.example.paymentbe.model.PaymentMethod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentStrategyFactoryTest {
    private final BankTransferStrategy bankTransferStrategy = new BankTransferStrategy();
    private final CreditCardStrategy creditCardStrategy = new CreditCardStrategy();
    
    private final PaymentStrategyFactory factory = 
            new PaymentStrategyFactory(bankTransferStrategy, creditCardStrategy);

    @Test
    void shouldReturnBankTransferStrategy() {
        PaymentStrategy strategy = factory.getStrategy(PaymentMethod.BANK_TRANSFER);
        assertTrue(strategy instanceof BankTransferStrategy);
    }

    @Test
    void shouldReturnCreditCardStrategy() {
        PaymentStrategy strategy = factory.getStrategy(PaymentMethod.CREDIT_CARD);
        assertTrue(strategy instanceof CreditCardStrategy);
    }
}