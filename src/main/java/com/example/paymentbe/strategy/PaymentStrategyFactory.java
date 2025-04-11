package com.example.paymentbe.strategy;

import com.example.paymentbe.model.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PaymentStrategyFactory {

    private final BankTransferStrategy bankTransferStrategy;
    private final CreditCardStrategy creditCardStrategy;

    public PaymentStrategyFactory(BankTransferStrategy bankTransferStrategy,
                                  CreditCardStrategy creditCardStrategy) {
        this.bankTransferStrategy = bankTransferStrategy;
        this.creditCardStrategy = creditCardStrategy;
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        return switch (method) {
            case BANK_TRANSFER -> bankTransferStrategy;
            case CREDIT_CARD -> creditCardStrategy;
        };
    }
}
