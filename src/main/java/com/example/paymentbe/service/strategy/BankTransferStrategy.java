package com.example.paymentbe.service.strategy;

import com.example.paymentbe.dto.PaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class BankTransferStrategy implements PaymentStrategy {
    @Override
    public boolean process(PaymentRequest request) {
        return request.getBankAccount() != null && 
               request.getBankAccount().matches("^[0-9]{10,20}$");
    }
}