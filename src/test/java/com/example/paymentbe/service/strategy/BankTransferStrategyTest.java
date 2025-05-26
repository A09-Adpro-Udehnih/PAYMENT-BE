package com.example.paymentbe.service.strategy;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.enums.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BankTransferStrategyTest {

    private BankTransferStrategy strategy;
    private PaymentRequest request;

    @BeforeEach
    void setUp() {
        strategy = new BankTransferStrategy();
        request = new PaymentRequest();
        request.setUserId(UUID.randomUUID());
        request.setCourseId(UUID.randomUUID());
        request.setMethod(PaymentMethod.BANK_TRANSFER);
        request.setAmount(100.0);
    }

    @Test
    void process_ValidBankAccount_ReturnsTrue() {
        request.setBankAccount("1234567890");
        assertTrue(strategy.process(request));
    }

    @Test
    void process_NullBankAccount_ReturnsFalse() {
        request.setBankAccount(null);
        assertFalse(strategy.process(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123456789", // Too short
        "123456789012345678901", // Too long
        "abcd1234567890", // Contains letters
        "12345-67890", // Contains special characters
        " 1234567890 " // Contains spaces
    })
    void process_InvalidBankAccount_ReturnsFalse(String bankAccount) {
        request.setBankAccount(bankAccount);
        assertFalse(strategy.process(request));
    }

    @Test
    void process_ValidLongBankAccount_ReturnsTrue() {
        request.setBankAccount("12345678901234567890"); // 20 digits
        assertTrue(strategy.process(request));
    }
} 