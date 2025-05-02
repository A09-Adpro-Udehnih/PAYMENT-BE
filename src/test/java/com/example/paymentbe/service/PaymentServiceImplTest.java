package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.model.*;
import com.example.paymentbe.repository.PaymentRepository;
import com.example.paymentbe.service.strategy.PaymentStrategy;
import com.example.paymentbe.service.strategy.PaymentStrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStrategyFactory strategyFactory;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processPayment_ShouldReturnPending_ForBankTransfer() {
        PaymentRequest request = new PaymentRequest();
        request.setUserId("user1");
        request.setCourseId("course1");
        request.setCourseName("Course 1");
        request.setTutorName("Tutor 1");
        request.setAmount(100.0);
        request.setMethod(PaymentMethod.BANK_TRANSFER);

        PaymentStrategy mockStrategy = mock(PaymentStrategy.class);
        when(strategyFactory.getStrategy(PaymentMethod.BANK_TRANSFER)).thenReturn(mockStrategy);
        when(mockStrategy.processPayment(100.0)).thenReturn(true);
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processPayment(request);

        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertTrue(response.getMessage().contains("Waiting for bank transfer"));
    }
}