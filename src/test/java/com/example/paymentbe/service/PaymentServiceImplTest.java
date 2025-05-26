package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundRequest;
import com.example.paymentbe.enums.PaymentMethod;
import com.example.paymentbe.enums.PaymentStatus;
import com.example.paymentbe.model.Payment;
import com.example.paymentbe.model.Refund;
import com.example.paymentbe.repository.PaymentRepository;
import com.example.paymentbe.repository.RefundRepository;
import com.example.paymentbe.service.strategy.PaymentStrategy;
import com.example.paymentbe.service.strategy.PaymentStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    void processPayment_Success() {
        when(strategyFactory.getStrategy(any(PaymentMethod.class))).thenReturn(paymentStrategy);
        when(paymentStrategy.process(any(PaymentRequest.class))).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(testPaymentId);
            return payment;
        });

        PaymentResponse response = paymentService.processPayment(testPaymentRequest);

        assertEquals(PaymentStatus.PAID, response.getStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentStrategy).process(any(PaymentRequest.class));
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
        verify(paymentStrategy).process(any(PaymentRequest.class));
    }

    @Test
    void processPayment_BankTransfer_Success() {
        testPaymentRequest.setMethod(PaymentMethod.BANK_TRANSFER);
        testPaymentRequest.setBankAccount("1234567890");
        
        when(strategyFactory.getStrategy(any(PaymentMethod.class))).thenReturn(paymentStrategy);
        when(paymentStrategy.process(any(PaymentRequest.class))).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(testPaymentId);
            return payment;
        });

        PaymentResponse response = paymentService.processPayment(testPaymentRequest);

        assertEquals(PaymentStatus.PENDING, response.getStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentStrategy).process(any(PaymentRequest.class));
    }

    @Test
    void processPayment_InvalidRequest_NullUserId() {
        testPaymentRequest.setUserId(null);
        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(testPaymentRequest));
    }

    @Test
    void processPayment_InvalidRequest_NullCourseId() {
        testPaymentRequest.setCourseId(null);
        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(testPaymentRequest));
    }

    @Test
    void processPayment_InvalidRequest_NullMethod() {
        testPaymentRequest.setMethod(null);
        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(testPaymentRequest));
    }

    @Test
    void processPayment_InvalidRequest_ZeroAmount() {
        testPaymentRequest.setAmount(0.0);
        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(testPaymentRequest));
    }

    @Test
    void processPayment_InvalidRequest_NegativeAmount() {
        testPaymentRequest.setAmount(-100.0);
        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(testPaymentRequest));
    }

    @Test
    void updatePaymentStatus_Success() {
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentResponse response = paymentService.updatePaymentStatus(testPaymentId.toString(), "PAID");

        assertEquals(PaymentStatus.PAID, response.getStatus());
        verify(paymentRepository).findById(testPaymentId);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_PaymentNotFound() {
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.updatePaymentStatus(testPaymentId.toString(), "PAID"));

        assertEquals("Payment not found with ID: " + testPaymentId, exception.getMessage());
        verify(paymentRepository).findById(testPaymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_InvalidStatus() {
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> paymentService.updatePaymentStatus(testPaymentId.toString(), "INVALID_STATUS"));

        assertEquals("Invalid payment status: INVALID_STATUS", exception.getMessage());
        verify(paymentRepository).findById(testPaymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPayment_Success() {
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));

        PaymentResponse response = paymentService.getPayment(testPaymentId.toString());

        assertNotNull(response);
        assertEquals(testPaymentId.toString(), response.getPaymentId());
        assertEquals(testUserId, response.getUserId());
        assertEquals(testCourseId, response.getCourseId());
        assertEquals(100.0, response.getAmount());
        assertEquals(PaymentMethod.CREDIT_CARD.name(), response.getPaymentMethod());
        assertEquals(PaymentStatus.PAID, response.getStatus());
        verify(paymentRepository).findById(testPaymentId);
    }

    @Test
    void getPayment_WithRefund() {
        Refund refund = Refund.builder()
                .reason("Test refund reason")
                .build();
        testPayment.setRefund(refund);
        
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));

        PaymentResponse response = paymentService.getPayment(testPaymentId.toString());

        assertNotNull(response);
        assertEquals("Test refund reason", response.getRefundReason());
        verify(paymentRepository).findById(testPaymentId);
    }

    @Test
    void getPayment_NotFound() {
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.getPayment(testPaymentId.toString()));

        assertEquals("Payment not found with ID: " + testPaymentId, exception.getMessage());
        verify(paymentRepository).findById(testPaymentId);
    }

    @Test
    void getPendingPayments_Success() {
        List<Payment> pendingPayments = Arrays.asList(testPayment);
        when(paymentRepository.findByStatus(PaymentStatus.PENDING)).thenReturn(pendingPayments);

        List<PaymentResponse> responses = paymentService.getPendingPayments();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testPaymentId.toString(), responses.get(0).getPaymentId());
        verify(paymentRepository).findByStatus(PaymentStatus.PENDING);
    }

    @Test
    void getUserPayments_Success() {
        List<Payment> userPayments = Arrays.asList(testPayment);
        when(paymentRepository.findByUserId(testUserId)).thenReturn(userPayments);

        List<PaymentResponse> responses = paymentService.getUserPayments(testUserId.toString());

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testPaymentId.toString(), responses.get(0).getPaymentId());
        verify(paymentRepository).findByUserId(testUserId);
    }

    @Test
    void testPaymentEqualsAndHashCode() {
        Payment payment1 = Payment.builder()
                .id(testPaymentId)
                .userId(testUserId)
                .courseId(testCourseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .build();

        Payment payment2 = Payment.builder()
                .id(testPaymentId)
                .userId(testUserId)
                .courseId(testCourseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .build();

        Payment payment3 = Payment.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .courseId(testCourseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .build();

        assertEquals(payment1, payment2);
        assertEquals(payment1.hashCode(), payment2.hashCode());
        assertNotEquals(payment1, payment3);
        assertNotEquals(payment1.hashCode(), payment3.hashCode());
        assertNotEquals(payment1, null);
        assertNotEquals(payment1, "not a payment");
    }
}