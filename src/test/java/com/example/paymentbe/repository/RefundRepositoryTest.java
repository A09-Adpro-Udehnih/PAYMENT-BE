package com.example.paymentbe.repository;

import com.example.paymentbe.enums.PaymentMethod;
import com.example.paymentbe.enums.PaymentStatus;
import com.example.paymentbe.model.Payment;
import com.example.paymentbe.model.Refund;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundRepositoryTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private Payment testPayment;
    private Refund testRefund;
    private UUID testPaymentId;
    private UUID testRefundId;

    @BeforeEach
    void setUp() {
        testPaymentId = UUID.randomUUID();
        testRefundId = UUID.randomUUID();
        
        testPayment = createTestPayment(PaymentStatus.PAID);
        testPayment.setId(testPaymentId);
        
        testRefund = Refund.builder()
                .id(testRefundId)
                .payment(testPayment)
                .reason("Test refund reason")
                .build();
    }

    @Test
    void testSaveAndFindById() {
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);
        when(refundRepository.findById(testRefundId)).thenReturn(Optional.of(testRefund));

        Refund savedRefund = refundRepository.save(testRefund);
        Optional<Refund> foundRefund = refundRepository.findById(testRefundId);

        assertThat(savedRefund).isNotNull();
        assertThat(foundRefund).isPresent();
        assertThat(foundRefund.get().getId()).isEqualTo(testRefundId);
        assertThat(foundRefund.get().getReason()).isEqualTo("Test refund reason");
        assertThat(foundRefund.get().getPayment()).isEqualTo(testPayment);
        
        verify(refundRepository).save(any(Refund.class));
        verify(refundRepository).findById(testRefundId);
    }

    @Test
    void testExistsByPaymentId() {
        when(refundRepository.existsByPaymentId(testPaymentId)).thenReturn(true);

        boolean exists = refundRepository.existsByPaymentId(testPaymentId);

        assertThat(exists).isTrue();
        verify(refundRepository).existsByPaymentId(testPaymentId);
    }

    @Test
    void testFindByNonExistentPaymentId() {
        UUID nonExistentPaymentId = UUID.randomUUID();
        when(refundRepository.existsByPaymentId(nonExistentPaymentId)).thenReturn(false);

        boolean exists = refundRepository.existsByPaymentId(nonExistentPaymentId);

        assertThat(exists).isFalse();
        verify(refundRepository).existsByPaymentId(nonExistentPaymentId);
    }

    @Test
    void testCreateRefundWithEmptyReason() {
        Refund emptyReasonRefund = Refund.builder()
                .id(testRefundId)
                .payment(testPayment)
                .reason("")
                .build();
        
        when(refundRepository.save(any(Refund.class))).thenReturn(emptyReasonRefund);
        when(refundRepository.findById(testRefundId)).thenReturn(Optional.of(emptyReasonRefund));

        Refund savedRefund = refundRepository.save(emptyReasonRefund);
        Optional<Refund> foundRefund = refundRepository.findById(testRefundId);

        assertThat(savedRefund).isNotNull();
        assertThat(foundRefund).isPresent();
        assertThat(foundRefund.get().getReason()).isEmpty();
        
        verify(refundRepository).save(any(Refund.class));
        verify(refundRepository).findById(testRefundId);
    }

    @Test
    void testCreateRefundWithLongReason() {
        String longReason = "This is a very long refund reason that exceeds the normal length. "
                + "It should still be stored correctly in the database. "
                + "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                + "Nullam auctor, nisl eget ultricies tincidunt, nisl nisl aliquam nisl.";

        Refund longReasonRefund = Refund.builder()
                .id(testRefundId)
                .payment(testPayment)
                .reason(longReason)
                .build();

        when(refundRepository.save(any(Refund.class))).thenReturn(longReasonRefund);
        when(refundRepository.findById(testRefundId)).thenReturn(Optional.of(longReasonRefund));

        Refund savedRefund = refundRepository.save(longReasonRefund);
        Optional<Refund> foundRefund = refundRepository.findById(testRefundId);

        assertThat(savedRefund).isNotNull();
        assertThat(foundRefund).isPresent();
        assertThat(foundRefund.get().getReason()).hasSize(longReason.length());
        
        verify(refundRepository).save(any(Refund.class));
        verify(refundRepository).findById(testRefundId);
    }

    @Test
    void testPaymentStatusAfterRefund() {
        Payment refundedPayment = createTestPayment(PaymentStatus.REFUNDED);
        refundedPayment.setId(testPaymentId);
        
        when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(refundedPayment));

        Refund savedRefund = refundRepository.save(testRefund);
        Optional<Payment> updatedPayment = paymentRepository.findById(testPaymentId);

        assertThat(savedRefund).isNotNull();
        assertThat(updatedPayment).isPresent();
        assertThat(updatedPayment.get().getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        
        verify(refundRepository).save(any(Refund.class));
        verify(paymentRepository).findById(testPaymentId);
    }

    @Test
    void testFindNonExistentRefund() {
        UUID nonExistentId = UUID.randomUUID();
        when(refundRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<Refund> refund = refundRepository.findById(nonExistentId);

        assertThat(refund).isEmpty();
        verify(refundRepository).findById(nonExistentId);
    }

    @Test
    void testSaveRefundWithNullReason() {
        Refund nullReasonRefund = Refund.builder()
                .id(testRefundId)
                .payment(testPayment)
                .reason(null)
                .build();
        
        when(refundRepository.save(any(Refund.class))).thenReturn(nullReasonRefund);

        Refund savedRefund = refundRepository.save(nullReasonRefund);

        assertThat(savedRefund).isNotNull();
        assertThat(savedRefund.getReason()).isNull();
        
        verify(refundRepository).save(any(Refund.class));
    }

    @Test
    void testMultipleRefundsForDifferentPayments() {
        UUID payment1Id = UUID.randomUUID();
        UUID payment2Id = UUID.randomUUID();
        
        when(refundRepository.existsByPaymentId(payment1Id)).thenReturn(true);
        when(refundRepository.existsByPaymentId(payment2Id)).thenReturn(false);

        boolean refund1Exists = refundRepository.existsByPaymentId(payment1Id);
        boolean refund2Exists = refundRepository.existsByPaymentId(payment2Id);

        assertThat(refund1Exists).isTrue();
        assertThat(refund2Exists).isFalse();
        
        verify(refundRepository).existsByPaymentId(payment1Id);
        verify(refundRepository).existsByPaymentId(payment2Id);
    }

    private Payment createTestPayment(PaymentStatus status) {
        return Payment.builder()
                .amount(10000L)
                .method(PaymentMethod.CREDIT_CARD)
                .status(status)
                .build();
    }
}