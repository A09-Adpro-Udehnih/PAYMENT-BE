package com.example.paymentbe.service.strategy;

import com.example.paymentbe.enums.PaymentMethod;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class PaymentStrategyFactory {
    private final Map<PaymentMethod, PaymentStrategy> strategies;

    public PaymentStrategyFactory(BankTransferStrategy bankTransferStrategy,
                                CreditCardStrategy creditCardStrategy) {
        this.strategies = new EnumMap<>(PaymentMethod.class);
        this.strategies.put(PaymentMethod.BANK_TRANSFER, bankTransferStrategy);
        this.strategies.put(PaymentMethod.CREDIT_CARD, creditCardStrategy);
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("Invalid payment method");
        }
        return strategy;
    }
}