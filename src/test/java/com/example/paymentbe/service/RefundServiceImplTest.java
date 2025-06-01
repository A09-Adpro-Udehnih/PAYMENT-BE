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
        when(paymentRepository.findById(paymentUUID)).thenReturn(Optional.of(mockPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(refundRepository.save(any(Refund.class))).thenReturn(mockRefund);

        RefundResponse result = refundService.requestRefund(paymentId, refundRequest);

        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId().toString());
        assertEquals("Product not as described", result.getReason());
        assertEquals(RefundStatus.PENDING, result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getRequestedAt());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals(PaymentStatus.REFUND_REQUESTED, paymentCaptor.getValue().getStatus());

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
        when(paymentRepository.findById(paymentUUID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> refundService.requestRefund(paymentId, refundRequest));
        
        assertEquals("Payment not found with ID: " + paymentId, exception.getMessage());
        
        verify(paymentRepository, times(1)).findById(paymentUUID);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void requestRefund_NotPaidPayment_ThrowsException() {
        mockPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(paymentUUID)).thenReturn(Optional.of(mockPayment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> refundService.requestRefund(paymentId, refundRequest));
        
        assertEquals("Only PAID payments can be refunded", exception.getMessage());
        
        verify(paymentRepository, times(1)).findById(paymentUUID);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void requestRefund_ExistingRefund_ThrowsException() {
        when(paymentRepository.findById(paymentUUID)).thenReturn(Optional.of(mockPayment));
        when(refundRepository.existsByPaymentId(paymentUUID)).thenReturn(true);

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
        List<Refund> mockRefunds = Arrays.asList(mockRefund);
        when(refundRepository.findByStatus(RefundStatus.PENDING)).thenReturn(mockRefunds);

        List<RefundResponse> result = refundService.getPendingRefunds();

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
        UUID refundId = UUID.randomUUID();
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(mockRefund));

        RefundResponse result = refundService.getRefund(refundId.toString());

        assertNotNull(result);
        assertEquals(mockRefund.getId().toString(), result.getId().toString());
        assertEquals(mockRefund.getPayment().getId().toString(), result.getPaymentId().toString());
        assertEquals(mockRefund.getReason(), result.getReason());
        assertEquals(mockRefund.getStatus(), result.getStatus());
        
        verify(refundRepository, times(1)).findById(refundId);
    }

    @Test
    void getRefund_NotFound_ThrowsException() {
        UUID refundId = UUID.randomUUID();
        when(refundRepository.findById(refundId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> refundService.getRefund(refundId.toString()));
        
        assertEquals("Refund not found with ID: " + refundId, exception.getMessage());
        
        verify(refundRepository, times(1)).findById(refundId);
    }

    @Test
    void testRefundEqualsAndHashCode() {
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

        assertEquals(refund1, refund2);
        assertEquals(refund1.hashCode(), refund2.hashCode());
        assertNotEquals(refund1, refund3);
        assertNotEquals(refund1.hashCode(), refund3.hashCode());
    }

    @Test
    void processRefund_Success_Accepted() {
        UUID refundId = UUID.randomUUID();
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(mockRefund));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(refundRepository.save(any(Refund.class))).thenReturn(mockRefund);

        RefundResponse result = refundService.processRefund(refundId.toString(), "ACCEPTED", "admin");

        assertNotNull(result);
        assertEquals(RefundStatus.ACCEPTED, result.getStatus());
        assertEquals("admin", result.getProcessedBy());
        
        verify(refundRepository).findById(refundId);
        verify(paymentRepository).save(any(Payment.class));
        verify(refundRepository).save(any(Refund.class));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals(PaymentStatus.REFUNDED, paymentCaptor.getValue().getStatus());
    }

    @Test
    void processRefund_Success_Rejected() {
        UUID refundId = UUID.randomUUID();
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(mockRefund));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);
        when(refundRepository.save(any(Refund.class))).thenReturn(mockRefund);

        RefundResponse result = refundService.processRefund(refundId.toString(), "REJECTED", "admin");

        assertNotNull(result);
        assertEquals(RefundStatus.REJECTED, result.getStatus());
        assertEquals("admin", result.getProcessedBy());
        
        verify(refundRepository).findById(refundId);
        verify(paymentRepository).save(any(Payment.class));
        verify(refundRepository).save(any(Refund.class));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals(PaymentStatus.PAID, paymentCaptor.getValue().getStatus());
    }

    @Test
    void processRefund_NotFound() {
        UUID refundId = UUID.randomUUID();
        when(refundRepository.findById(refundId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> refundService.processRefund(refundId.toString(), "ACCEPTED", "admin"));

        assertEquals("Refund not found with ID: " + refundId, exception.getMessage());
        verify(refundRepository).findById(refundId);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void processRefund_NotPending() {
        UUID refundId = UUID.randomUUID();
        mockRefund.setStatus(RefundStatus.ACCEPTED);
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(mockRefund));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> refundService.processRefund(refundId.toString(), "ACCEPTED", "admin"));

        assertEquals("Only PENDING refunds can be processed", exception.getMessage());
        verify(refundRepository).findById(refundId);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void processRefund_InvalidStatus() {
        // Given
        UUID refundId = UUID.randomUUID();
        when(refundRepository.findById(refundId)).thenReturn(Optional.of(mockRefund));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> refundService.processRefund(refundId.toString(), "INVALID_STATUS", "admin"));

        verify(refundRepository).findById(refundId);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void testPaymentEqualsAndHashCode() {
        UUID sharedId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        String paymentRef = "PAY-12345678";
        
        Payment payment1 = Payment.builder()
                .id(sharedId)
                .userId(userId)
                .courseId(courseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .paymentReference(paymentRef)
                .createdAt(now)
                .updatedAt(now)
                .build();
                
        Payment payment2 = Payment.builder()
                .id(sharedId)
                .userId(userId)
                .courseId(courseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .paymentReference(paymentRef)
                .createdAt(now)
                .updatedAt(now)
                .build();
                
        Payment payment3 = Payment.builder()
                .id(UUID.randomUUID())  
                .userId(userId)
                .courseId(courseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .paymentReference(paymentRef)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(payment1, payment2);
        assertEquals(payment1.hashCode(), payment2.hashCode());
        assertNotEquals(payment1, payment3);
        assertNotEquals(payment1.hashCode(), payment3.hashCode());

        assertNotEquals(payment1, null);
        assertNotEquals(payment1, "not a payment");
    }

    @Test
    void testPaymentResponseBuilder() {
        PaymentResponse response = PaymentResponse.builder()
                .paymentId(mockPayment.getId().toString())
                .userId(mockPayment.getUserId())
                .courseId(mockPayment.getCourseId())
                .amount(mockPayment.getAmount())
                .paymentMethod(mockPayment.getMethod().name())
                .status(mockPayment.getStatus())
                .paymentReference(mockPayment.getPaymentReference())
                .createdAt(mockPayment.getCreatedAt())
                .updatedAt(mockPayment.getUpdatedAt())
                .build();

        assertNotNull(response);
        assertEquals(mockPayment.getId().toString(), response.getPaymentId());
        assertEquals(mockPayment.getUserId(), response.getUserId());
        assertEquals(mockPayment.getCourseId(), response.getCourseId());
        assertEquals(mockPayment.getAmount(), response.getAmount());
        assertEquals(mockPayment.getMethod().name(), response.getPaymentMethod());
        assertEquals(mockPayment.getStatus(), response.getStatus());
        assertEquals(mockPayment.getPaymentReference(), response.getPaymentReference());
        assertEquals(mockPayment.getCreatedAt(), response.getCreatedAt());
        assertEquals(mockPayment.getUpdatedAt(), response.getUpdatedAt());
    }

    @Test
    void testRefundResponseBuilder() {
        RefundResponse response = RefundResponse.builder()
                .id(mockRefund.getId())
                .paymentId(mockRefund.getPayment().getId())
                .reason(mockRefund.getReason())
                .processedBy(mockRefund.getProcessedBy())
                .status(mockRefund.getStatus())
                .createdAt(mockRefund.getCreatedAt())
                .requestedAt(mockRefund.getRequestedAt())
                .processedAt(mockRefund.getProcessedAt())
                .payment(buildPaymentResponse(mockRefund.getPayment()))
                .build();

        assertNotNull(response);
        assertEquals(mockRefund.getId(), response.getId());
        assertEquals(mockRefund.getPayment().getId(), response.getPaymentId());
        assertEquals(mockRefund.getReason(), response.getReason());
        assertEquals(mockRefund.getProcessedBy(), response.getProcessedBy());
        assertEquals(mockRefund.getStatus(), response.getStatus());
        assertEquals(mockRefund.getCreatedAt(), response.getCreatedAt());
        assertEquals(mockRefund.getRequestedAt(), response.getRequestedAt());
        assertEquals(mockRefund.getProcessedAt(), response.getProcessedAt());
        assertNotNull(response.getPayment());
    }

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

    private PaymentResponse buildPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId().toString())
                .userId(payment.getUserId())
                .courseId(payment.getCourseId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getMethod().name())
                .status(payment.getStatus())
                .paymentReference(payment.getPaymentReference())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}