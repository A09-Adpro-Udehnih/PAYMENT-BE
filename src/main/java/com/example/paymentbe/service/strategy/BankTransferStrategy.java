package com.example.paymentbe.service.strategy;

import org.springframework.stereotype.Service;

@Service
public class BankTransferStrategy implements PaymentStrategy {
    @Override
    public boolean processPayment(double amount) {
        // Bank transfer always succeeds in simulation
        // Real implementation would call bank API
        return amount > 0;
    }
}