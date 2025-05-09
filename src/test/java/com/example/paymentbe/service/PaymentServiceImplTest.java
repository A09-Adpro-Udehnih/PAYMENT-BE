package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundRequest;
import com.example.paymentbe.model.Payment;
import com.example.paymentbe.model.PaymentMethod;
import com.example.paymentbe.model.PaymentStatus;
import com.example.paymentbe.model.Refund;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    private Payment testPayment;
    private UUID testPaymentId;
    private PaymentRequest testPaymentRequest;
    private RefundRequest testRefundRequest;

    @BeforeEach
    void setUp() {
        testPaymentId = UUID.randomUUID();
        testPayment = Payment.builder()
                .id(testPaymentId)
                .userId("user123")
                .courseId("course123")
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .cardLastFour("1234")
                .paymentReference("PAY-12345678")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testPaymentRequest = new PaymentRequest();
        testPaymentRequest.setUserId("user123");
        testPaymentRequest.setCourseId("course123");
        testPaymentRequest.setAmount(100.0);
        testPaymentRequest.setMethod(PaymentMethod.CREDIT_CARD);
        testPaymentRequest.setCardNumber("4111111111111111");
        testPaymentRequest.setCardCvc("123");

        testRefundRequest = new RefundRequest();
        testRefundRequest.setReason("Not satisfied with the course");
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

    @Test
    void requestRefund_Success() {
        when(paymentRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPayment));
        
        PaymentResponse response = paymentService.requestRefund(testPaymentId.toString(), testRefundRequest);

        assertEquals(PaymentStatus.REFUND_REQUESTED, response.getStatus());
        verify(paymentRepository).save(testPayment);
        verify(refundRepository).save(any(Refund.class));
    }

    @Test
    void getPendingPayments_Success() {
        Payment pendingPayment = Payment.builder()
                .id(UUID.randomUUID())
                .userId("user456")
                .courseId("course456")
                .amount(200.0)
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.PENDING)
                .bankAccount("1234567890")
                .paymentReference("PAY-87654321")
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findByStatus(PaymentStatus.PENDING)).thenReturn(Arrays.asList(pendingPayment));

        List<PaymentResponse> responses = paymentService.getPendingPayments();

        assertEquals(1, responses.size());
        assertEquals(PaymentStatus.PENDING, responses.get(0).getStatus());
        assertEquals("user456", responses.get(0).getUserId());
    }

    @Test
    void getRefundRequests_Success() {
        Payment refundRequestedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .userId("user789")
                .courseId("course789")
                .amount(300.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.REFUND_REQUESTED)
                .cardLastFour("5678")
                .paymentReference("PAY-13579246")
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findByStatus(PaymentStatus.REFUND_REQUESTED)).thenReturn(Arrays.asList(refundRequestedPayment));

        List<PaymentResponse> responses = paymentService.getRefundRequests();

        assertEquals(1, responses.size());
        assertEquals(PaymentStatus.REFUND_REQUESTED, responses.get(0).getStatus());
        assertEquals("user789", responses.get(0).getUserId());
    }
}