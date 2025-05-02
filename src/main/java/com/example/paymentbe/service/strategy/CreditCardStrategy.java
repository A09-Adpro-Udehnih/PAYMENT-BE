package com.example.paymentbe.service.strategy;

import org.springframework.stereotype.Service;

@Service
public class CreditCardStrategy implements PaymentStrategy {
    @Override
    public boolean processPayment(double amount) {
        return amount > 0 && amount <= 1000000;
    }
}