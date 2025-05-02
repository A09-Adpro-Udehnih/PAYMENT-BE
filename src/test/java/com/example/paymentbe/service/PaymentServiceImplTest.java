package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.model.*;
import com.example.paymentbe.repository.PaymentRepository;
import com.example.paymentbe.service.strategy.PaymentStrategy;
import com.example.paymentbe.service.strategy.PaymentStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStrategyFactory strategyFactory;

    @Mock
    private PaymentStrategy paymentStrategy;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processPayment_ShouldReturnPending_ForBankTransfer() {
        PaymentRequest request = new PaymentRequest();
        request.setUserId("user1");
        request.setCourseId("course1");
        request.setCourseName("Course 1");
        request.setTutorName("Tutor 1");
        request.setAmount(100.0);
        request.setMethod(PaymentMethod.BANK_TRANSFER);

        when(strategyFactory.getStrategy(PaymentMethod.BANK_TRANSFER)).thenReturn(paymentStrategy);
        when(paymentStrategy.processPayment(100.0)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = paymentService.processPayment(request);

        assertEquals(PaymentStatus.PENDING, response.getStatus());
    }
}