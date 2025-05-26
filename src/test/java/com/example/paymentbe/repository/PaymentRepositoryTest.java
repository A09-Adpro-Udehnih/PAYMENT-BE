package com.example.paymentbe.repository;

import com.example.paymentbe.model.Payment;
import com.example.paymentbe.enums.PaymentMethod;
import com.example.paymentbe.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentRepositoryTest {

    @Mock
    private PaymentRepository paymentRepository;

    private Payment testPayment;
    private UUID testUserId;
    private UUID testCourseId;
    private UUID testPaymentId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCourseId = UUID.randomUUID();
        testPaymentId = UUID.randomUUID();
        testPayment = createTestPayment(testUserId, testCourseId);
        testPayment.setId(testPaymentId);
    }

    @Test
    public void testSaveAndFindById() {
        // Given
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));

        // When
        Payment savedPayment = paymentRepository.save(testPayment);
        Optional<Payment> foundPayment = paymentRepository.findById(testPaymentId);

        // Then
        assertThat(savedPayment).isNotNull();
        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get().getId()).isEqualTo(testPaymentId);
        assertThat(foundPayment.get().getUserId()).isEqualTo(testUserId);
        assertThat(foundPayment.get().getCourseId()).isEqualTo(testCourseId);
        
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentRepository).findById(testPaymentId);
    }

    @Test
    public void testFindByStatus() {
        // Given
        Payment pendingPayment = createTestPayment(testUserId, testCourseId);
        pendingPayment.setStatus(PaymentStatus.PENDING);
        
        Payment paidPayment = createTestPayment(UUID.randomUUID(), testCourseId);
        paidPayment.setStatus(PaymentStatus.PAID);

        when(paymentRepository.findByStatus(PaymentStatus.PENDING))
                .thenReturn(List.of(pendingPayment));
        when(paymentRepository.findByStatus(PaymentStatus.PAID))
                .thenReturn(List.of(paidPayment));

        // When
        List<Payment> pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);
        List<Payment> paidPayments = paymentRepository.findByStatus(PaymentStatus.PAID);

        // Then
        assertThat(pendingPayments).hasSize(1);
        assertThat(paidPayments).hasSize(1);
        assertThat(pendingPayments.get(0).getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(paidPayments.get(0).getStatus()).isEqualTo(PaymentStatus.PAID);
        
        verify(paymentRepository).findByStatus(PaymentStatus.PENDING);
        verify(paymentRepository).findByStatus(PaymentStatus.PAID);
    }

    @Test
    public void testFindByUserId() {
        // Given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        
        Payment payment1 = createTestPayment(userId1, testCourseId);
        Payment payment2 = createTestPayment(userId2, testCourseId);

        when(paymentRepository.findByUserId(userId1)).thenReturn(List.of(payment1));
        when(paymentRepository.findByUserId(userId2)).thenReturn(List.of(payment2));

        // When
        List<Payment> user1Payments = paymentRepository.findByUserId(userId1);
        List<Payment> user2Payments = paymentRepository.findByUserId(userId2);

        // Then
        assertThat(user1Payments).hasSize(1);
        assertThat(user2Payments).hasSize(1);
        assertThat(user1Payments.get(0).getUserId()).isEqualTo(userId1);
        assertThat(user2Payments.get(0).getUserId()).isEqualTo(userId2);
        
        verify(paymentRepository).findByUserId(userId1);
        verify(paymentRepository).findByUserId(userId2);
    }

    @Test
    public void testUpdatePayment() {
        // Given
        Payment originalPayment = createTestPayment(testUserId, testCourseId);
        originalPayment.setId(testPaymentId);
        originalPayment.setStatus(PaymentStatus.PENDING);

        Payment updatedPayment = createTestPayment(testUserId, testCourseId);
        updatedPayment.setId(testPaymentId);
        updatedPayment.setStatus(PaymentStatus.PAID);

        when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(updatedPayment));

        // When
        Payment savedPayment = paymentRepository.save(updatedPayment);
        Optional<Payment> foundPayment = paymentRepository.findById(testPaymentId);

        // Then
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get().getStatus()).isEqualTo(PaymentStatus.PAID);
        
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentRepository).findById(testPaymentId);
    }

    @Test
    public void testFindNonExistentPayment() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(paymentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Payment> payment = paymentRepository.findById(nonExistentId);

        // Then
        assertThat(payment).isEmpty();
        verify(paymentRepository).findById(nonExistentId);
    }

    @Test
    public void testFindByNonExistentUserId() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(paymentRepository.findByUserId(nonExistentUserId)).thenReturn(Collections.emptyList());

        // When
        List<Payment> payments = paymentRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(payments).isEmpty();
        verify(paymentRepository).findByUserId(nonExistentUserId);
    }

    @Test
    void testFindByNullStatus_returnsEmpty() {
        // Given
        when(paymentRepository.findByStatus(null)).thenReturn(Collections.emptyList());

        // When
        List<Payment> result = paymentRepository.findByStatus(null);

        // Then
        assertThat(result).isEmpty();
        verify(paymentRepository).findByStatus(null);
    }

    @Test
    void testSaveMultiplePayments() {
        // Given
        Payment payment1 = createTestPayment(UUID.randomUUID(), testCourseId);
        Payment payment2 = createTestPayment(UUID.randomUUID(), testCourseId);
        
        when(paymentRepository.save(payment1)).thenReturn(payment1);
        when(paymentRepository.save(payment2)).thenReturn(payment2);

        // When
        Payment saved1 = paymentRepository.save(payment1);
        Payment saved2 = paymentRepository.save(payment2);

        // Then
        assertThat(saved1).isEqualTo(payment1);
        assertThat(saved2).isEqualTo(payment2);
        
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void testFindByStatusWithMultipleResults() {
        // Given
        Payment payment1 = createTestPayment(UUID.randomUUID(), testCourseId);
        Payment payment2 = createTestPayment(UUID.randomUUID(), testCourseId);
        payment1.setStatus(PaymentStatus.PENDING);
        payment2.setStatus(PaymentStatus.PENDING);
        
        List<Payment> pendingPayments = List.of(payment1, payment2);
        when(paymentRepository.findByStatus(PaymentStatus.PENDING)).thenReturn(pendingPayments);

        // When
        List<Payment> result = paymentRepository.findByStatus(PaymentStatus.PENDING);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(payment1, payment2);
        verify(paymentRepository).findByStatus(PaymentStatus.PENDING);
    }

    private Payment createTestPayment(UUID userId, UUID courseId) {
        return Payment.builder()
                .userId(userId)
                .courseId(courseId)
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .cardLastFour("1234")
                .paymentReference("TEST-REF")
                .createdAt(LocalDateTime.now())
                .build();
    }
}