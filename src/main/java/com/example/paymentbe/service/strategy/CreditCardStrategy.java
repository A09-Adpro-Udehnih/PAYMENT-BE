package com.example.paymentbe.service.strategy;

import com.example.paymentbe.dto.PaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class CreditCardStrategy implements PaymentStrategy {
    @Override
    public boolean process(PaymentRequest request) {
        // Simulasi validasi kartu kredit sederhana
        String cardNumber = request.getCardNumber();
        String cvc = request.getCardCvc();
        
        boolean validCard = cardNumber != null && 
                          cardNumber.matches("^[0-9]{13,16}$") && 
                          cvc != null && 
                          cvc.matches("^[0-9]{3,4}$");
        
        return validCard && request.getAmount() > 0;
    }
}