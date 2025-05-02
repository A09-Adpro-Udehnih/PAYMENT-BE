package com.example.paymentbe.service.strategy;

public interface PaymentStrategy {
    boolean processPayment(double amount);
}