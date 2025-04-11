package com.example.paymentbe.strategy;

import org.springframework.stereotype.Component;

@Component
public class CreditCardStrategy implements PaymentStrategy {

    @Override
    public boolean pay(double amount) {
        return amount > 0 && amount < 1000000; // batas tertentu
    }
}
