package com.example.paymentbe.service.strategy;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.enums.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreditCardStrategyTest {

    private CreditCardStrategy strategy;
    private PaymentRequest request;

    @BeforeEach
    void setUp() {
        strategy = new CreditCardStrategy();
        request = new PaymentRequest();
        request.setUserId(UUID.randomUUID());
        request.setCourseId(UUID.randomUUID());
        request.setMethod(PaymentMethod.CREDIT_CARD);
        request.setAmount(100.0);
    }

    @Test
    void process_ValidCardDetails_ReturnsTrue() {
        request.setCardNumber("4111111111111111");
        request.setCardCvc("123");
        
        assertTrue(strategy.process(request));
    }

    @Test
    void process_NullCardNumber_ReturnsFalse() {
        request.setCardNumber(null);
        request.setCardCvc("123");
        
        assertFalse(strategy.process(request));
    }

    @Test
    void process_NullCvc_ReturnsFalse() {
        request.setCardNumber("4111111111111111");
        request.setCardCvc(null);
        
        assertFalse(strategy.process(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "12345678901", "abcd1234567890"})
    void process_InvalidCardNumber_ReturnsFalse(String cardNumber) {
        request.setCardNumber(cardNumber);
        request.setCardCvc("123");
        
        assertFalse(strategy.process(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12", "12345", "abc"})
    void process_InvalidCvc_ReturnsFalse(String cvc) {
        request.setCardNumber("4111111111111111");
        request.setCardCvc(cvc);
        
        assertFalse(strategy.process(request));
    }

    @Test
    void process_ZeroAmount_ReturnsFalse() {
        request.setCardNumber("4111111111111111");
        request.setCardCvc("123");
        request.setAmount(0.0);
        
        assertFalse(strategy.process(request));
    }

    @Test
    void process_NegativeAmount_ReturnsFalse() {
        request.setCardNumber("4111111111111111");
        request.setCardCvc("123");
        request.setAmount(-100.0);
        
        assertFalse(strategy.process(request));
    }
} 