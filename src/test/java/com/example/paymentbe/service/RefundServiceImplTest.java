package com.example.paymentbe.service;

import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundRequest;
import com.example.paymentbe.dto.RefundResponse;
import com.example.paymentbe.enums.PaymentMethod;
import com.example.paymentbe.enums.PaymentStatus;
import com.example.paymentbe.enums.RefundStatus;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        RefundResponse result = refundService.requestRefund(paymentId, refundRequest);

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId().toString());
        assertEquals("Product not as described", result.getReason());
        assertEquals(RefundStatus.PENDING, result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getRequestedAt());

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
    void requestRefund_NotPaidPayment_ThrowsException() {
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
    void requestRefund_ExistingRefund_ThrowsException() {
        // Given
        when(paymentRepository.findById(paymentUUID)).thenReturn(Optional.of(mockPayment));
        when(refundRepository.existsByPaymentId(paymentUUID)).thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> refundService.requestRefund(paymentId, refundRequest));
        
        assertEquals("Refund request already exists for this payment", exception.getMessage());
        
        verify(paymentRepository, times(1)).findById(paymentUUID);
        verify(refundRepository, times(1)).existsByPaymentId(paymentUUID);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void getPendingRefunds_Success() {
        // Given
        List<Refund> mockRefunds = Arrays.asList(mockRefund);
        when(refundRepository.findByStatus(RefundStatus.PENDING)).thenReturn(mockRefunds);

        // When
        List<RefundResponse> result = refundService.getPendingRefunds();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        RefundResponse response = result.get(0);
        assertEquals(mockRefund.getId().toString(), response.getId().toString());
        assertEquals(mockRefund.getPayment().getId().toString(), response.getPaymentId().toString());
        assertEquals(mockRefund.getReason(), response.getReason());
        assertEquals(mockRefund.getStatus(), response.getStatus());
        
        verify(refundRepository, times(1)).findByStatus(RefundStatus.PENDING);
    }

    @Test
    void getRefund_Success() {
        // Given
        UUID refundId = UUID.randomUUID();
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(mockRefund));

        // When
        RefundResponse result = refundService.getRefund(refundId.toString());

        // Then
        assertNotNull(result);
        assertEquals(mockRefund.getId().toString(), result.getId().toString());
        assertEquals(mockRefund.getPayment().getId().toString(), result.getPaymentId().toString());
        assertEquals(mockRefund.getReason(), result.getReason());
        assertEquals(mockRefund.getStatus(), result.getStatus());
        
        verify(refundRepository, times(1)).findById(refundId);
    }

    @Test
    void getRefund_NotFound_ThrowsException() {
        // Given
        UUID refundId = UUID.randomUUID();
        when(refundRepository.findById(refundId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> refundService.getRefund(refundId.toString()));
        
        assertEquals("Refund not found with ID: " + refundId, exception.getMessage());
        
        verify(refundRepository, times(1)).findById(refundId);
    }

    @Test
    void testRefundEqualsAndHashCode() {
        // Arrange
        UUID sharedId = UUID.randomUUID();
        Payment payment = new Payment();
        LocalDateTime now = LocalDateTime.now();
        
        Refund refund1 = Refund.builder()
                .id(sharedId)
                .payment(payment)
                .reason("Reason 1")
                .processedBy("admin1")
                .status(RefundStatus.PENDING)
                .createdAt(now)
                .requestedAt(now)
                .build();
                
        Refund refund2 = Refund.builder()
                .id(sharedId)
                .payment(payment)
                .reason("Reason 1")
                .processedBy("admin1")
                .status(RefundStatus.PENDING)
                .createdAt(now)
                .requestedAt(now)
                .build();
                
        Refund refund3 = Refund.builder()
                .id(UUID.randomUUID())
                .payment(payment)
                .reason("Reason 2")
                .processedBy("admin2")
                .status(RefundStatus.ACCEPTED)
                .createdAt(now)
                .requestedAt(now)
                .build();

        // Assert
        assertEquals(refund1, refund2);
        assertEquals(refund1.hashCode(), refund2.hashCode());
        assertNotEquals(refund1, refund3);
        assertNotEquals(refund1.hashCode(), refund3.hashCode());
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
                .id(UUID.randomUUID())
                .payment(mockPayment)
                .reason("Product not as described")
                .status(RefundStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .requestedAt(LocalDateTime.now())
                .build();
    }
}