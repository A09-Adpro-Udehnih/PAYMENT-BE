package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundRequest;
import com.example.paymentbe.enums.PaymentStatus;
import com.example.paymentbe.enums.PaymentMethod;
import com.example.paymentbe.model.Payment;
import com.example.paymentbe.model.Refund;
import com.example.paymentbe.repository.PaymentRepository;
import com.example.paymentbe.repository.RefundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @InjectMocks
    private RefundServiceImpl refundService;

    private UUID paymentUUID;
    private String paymentId;
    private Payment mockPayment;
    private RefundRequest refundRequest;
    private Refund mockRefund;

    @BeforeEach
    void setUp() {
        paymentUUID = UUID.randomUUID();
        paymentId = paymentUUID.toString();
        
        mockPayment = createMockPayment();
        refundRequest = createMockRefundRequest();
        mockRefund = createMockRefund();
    }

    @Test
    void requestRefund_Success() {
        // Given
        when(paymentRepository.findById(paymentUUID)).thenReturn(Optional.of(mockPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(refundRepository.save(any(Refund.class))).thenReturn(mockRefund);

        // When
        PaymentResponse result = refundService.requestRefund(paymentId, refundRequest);

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        assertEquals(mockPayment.getUserId(), result.getUserId());
        assertEquals(mockPayment.getCourseId(), result.getCourseId());
        assertEquals(100.0, result.getAmount());
        assertEquals("CREDIT_CARD", result.getPaymentMethod());
        assertEquals(PaymentStatus.REFUND_REQUESTED, result.getStatus());
        assertEquals("PAY_REF_123", result.getPaymentReference());
        assertEquals("Product not as described", result.getRefundReason());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        // Verify payment status was updated
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals(PaymentStatus.REFUND_REQUESTED, paymentCaptor.getValue().getStatus());

        // Verify refund was created
        ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
        verify(refundRepository).save(refundCaptor.capture());
        assertEquals(mockPayment, refundCaptor.getValue().getPayment());
        assertEquals("Product not as described", refundCaptor.getValue().getReason());
        assertNotNull(refundCaptor.getValue().getCreatedAt());

        verify(paymentRepository, times(1)).findById(paymentUUID);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(refundRepository, times(1)).save(any(Refund.class));
    }

    @Test
    void requestRefund_PaymentNotFound_ThrowsException() {
        // Given
        when(paymentRepository.findById(paymentUUID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> refundService.requestRefund(paymentId, refundRequest));
        
        assertEquals("Payment not found with ID: " + paymentId, exception.getMessage());
        
        verify(paymentRepository, times(1)).findById(paymentUUID);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void requestRefund_InvalidPaymentId_ThrowsException() {
        // Given
        String invalidPaymentId = "invalid-uuid";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> refundService.requestRefund(invalidPaymentId, refundRequest));
        
        assertTrue(exception.getMessage().contains("Invalid UUID string"));
        
        verify(paymentRepository, never()).findById(any(UUID.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void requestRefund_PaymentStatusNotPaid_ThrowsException() {
        // Given
        mockPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(paymentUUID)).thenReturn(Optional.of(mockPayment));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> refundService.requestRefund(paymentId, refundRequest));
        
        assertEquals("Only PAID payments can be refunded", exception.getMessage());
        
        verify(paymentRepository, times(1)).findById(paymentUUID);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void requestRefund_PaymentStatusRefunded_ThrowsException() {
        // Given
        mockPayment.setStatus(PaymentStatus.REFUNDED);
        when(paymentRepository.findById(paymentUUID)).thenReturn(Optional.of(mockPayment));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> refundService.requestRefund(paymentId, refundRequest));
        
        assertEquals("Only PAID payments can be refunded", exception.getMessage());
        
        verify(paymentRepository, times(1)).findById(paymentUUID);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void requestRefund_PaymentStatusFailed_ThrowsException() {
        // Given
        mockPayment.setStatus(PaymentStatus.FAILED);
        when(paymentRepository.findById(paymentUUID)).thenReturn(Optional.of(mockPayment));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> refundService.requestRefund(paymentId, refundRequest));
        
        assertEquals("Only PAID payments can be refunded", exception.getMessage());
        
        verify(paymentRepository, times(1)).findById(paymentUUID);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void getRefundRequests_WithData_Success() {
        // Given
        Payment payment1 = createMockPayment();
        payment1.setStatus(PaymentStatus.REFUND_REQUESTED);
        payment1.setRefund(mockRefund);
        
        Payment payment2 = createMockPayment();
        payment2.setId(UUID.randomUUID());
        payment2.setStatus(PaymentStatus.REFUND_REQUESTED);
        payment2.setUserId(UUID.randomUUID());
        payment2.setCourseId(UUID.randomUUID());
        payment2.setRefund(mockRefund);

        List<Payment> mockPayments = Arrays.asList(payment1, payment2);
        when(paymentRepository.findByStatus(PaymentStatus.REFUND_REQUESTED)).thenReturn(mockPayments);

        // When
        List<PaymentResponse> result = refundService.getRefundRequests();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        PaymentResponse response1 = result.get(0);
        assertEquals(payment1.getId().toString(), response1.getPaymentId());
        assertEquals(payment1.getUserId(), response1.getUserId());
        assertEquals(payment1.getCourseId(), response1.getCourseId());
        assertEquals(100.0, response1.getAmount());
        assertEquals("CREDIT_CARD", response1.getPaymentMethod());
        assertEquals(PaymentStatus.REFUND_REQUESTED, response1.getStatus());
        assertEquals("Product not as described", response1.getRefundReason());

        PaymentResponse response2 = result.get(1);
        assertEquals(payment2.getId().toString(), response2.getPaymentId());
        assertEquals(payment2.getUserId(), response2.getUserId());
        assertEquals(payment2.getCourseId(), response2.getCourseId());
        assertEquals("Product not as described", response2.getRefundReason());

        verify(paymentRepository, times(1)).findByStatus(PaymentStatus.REFUND_REQUESTED);
    }

    @Test
    void getRefundRequests_EmptyList_Success() {
        // Given
        when(paymentRepository.findByStatus(PaymentStatus.REFUND_REQUESTED)).thenReturn(Collections.emptyList());

        // When
        List<PaymentResponse> result = refundService.getRefundRequests();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(paymentRepository, times(1)).findByStatus(PaymentStatus.REFUND_REQUESTED);
    }

    @Test
    void getRefundRequests_WithNullRefund_Success() {
        // Given
        Payment payment = createMockPayment();
        payment.setStatus(PaymentStatus.REFUND_REQUESTED);
        payment.setRefund(null); // No refund object

        List<Payment> mockPayments = Arrays.asList(payment);
        when(paymentRepository.findByStatus(PaymentStatus.REFUND_REQUESTED)).thenReturn(mockPayments);

        // When
        List<PaymentResponse> result = refundService.getRefundRequests();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        PaymentResponse response = result.get(0);
        assertEquals(payment.getId().toString(), response.getPaymentId());
        assertNull(response.getRefundReason()); // Should be null when no refund object
        
        verify(paymentRepository, times(1)).findByStatus(PaymentStatus.REFUND_REQUESTED);
    }

    @Test
    void getRefundRequests_WithRefundButNullReason_Success() {
        // Given
        Payment payment = createMockPayment();
        payment.setStatus(PaymentStatus.REFUND_REQUESTED);
        
        Refund refundWithNullReason = Refund.builder()
                .payment(payment)
                .reason(null) // Null reason
                .createdAt(LocalDateTime.now())
                .build();
        payment.setRefund(refundWithNullReason);

        List<Payment> mockPayments = Arrays.asList(payment);
        when(paymentRepository.findByStatus(PaymentStatus.REFUND_REQUESTED)).thenReturn(mockPayments);

        // When
        List<PaymentResponse> result = refundService.getRefundRequests();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        PaymentResponse response = result.get(0);
        assertEquals(payment.getId().toString(), response.getPaymentId());
        assertNull(response.getRefundReason()); // Should be null when reason is null
        
        verify(paymentRepository, times(1)).findByStatus(PaymentStatus.REFUND_REQUESTED);
    }

    // Helper methods
    private Payment createMockPayment() {
        Payment payment = new Payment();
        payment.setId(paymentUUID);
        payment.setUserId(UUID.randomUUID());
        payment.setCourseId(UUID.randomUUID());
        payment.setAmount(100.0);
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentReference("PAY_REF_123");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return payment;
    }

    private RefundRequest createMockRefundRequest() {
        RefundRequest request = new RefundRequest();
        request.setReason("Product not as described");
        return request;
    }

    private Refund createMockRefund() {
        return Refund.builder()
                .payment(mockPayment)
                .reason("Product not as described")
                .createdAt(LocalDateTime.now())
                .build();
    }
}