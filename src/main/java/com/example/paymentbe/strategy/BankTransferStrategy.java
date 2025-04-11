package com.example.paymentbe.strategy;

import org.springframework.stereotype.Component;

@Component
public class BankTransferStrategy implements PaymentStrategy {

    @Override
    public boolean pay(double amount) {
        return amount > 0;
    }
}
