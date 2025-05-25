package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundRequest;
import com.example.paymentbe.enums.PaymentMethod;
import com.example.paymentbe.enums.PaymentStatus;
import com.example.paymentbe.model.Payment;
import com.example.paymentbe.repository.PaymentRepository;
import com.example.paymentbe.repository.RefundRepository;
import com.example.paymentbe.service.strategy.PaymentStrategy;
import com.example.paymentbe.service.strategy.PaymentStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentStrategyFactory strategyFactory;

    @Mock
    private PaymentStrategy paymentStrategy;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID testPaymentId;
    private UUID testUserId;
    private UUID testCourseId;
    private Payment testPayment;
    private PaymentRequest testPaymentRequest;

    @BeforeEach
    void setUp() {
        testPaymentId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testCourseId = UUID.randomUUID();

        testPayment = Payment.builder()
                .id(testPaymentId)
                .userId(testUserId)
                .courseId(testCourseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .cardLastFour("1234")
                .paymentReference("PAY-12345678")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testPaymentRequest = new PaymentRequest();
        testPaymentRequest.setUserId(testUserId);
        testPaymentRequest.setCourseId(testCourseId);
        testPaymentRequest.setAmount(100.0);
        testPaymentRequest.setMethod(PaymentMethod.CREDIT_CARD);
        testPaymentRequest.setCardNumber("4111111111111111");
        testPaymentRequest.setCardCvc("123");
    }

    @Test
    void processPayment_Failed() {
        when(strategyFactory.getStrategy(any(PaymentMethod.class))).thenReturn(paymentStrategy);
        when(paymentStrategy.process(any(PaymentRequest.class))).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(testPaymentId);
            return payment;
        });

        PaymentResponse response = paymentService.processPayment(testPaymentRequest);

        assertEquals(PaymentStatus.FAILED, response.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }
}