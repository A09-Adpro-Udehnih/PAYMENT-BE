package com.example.paymentbe.repository;

import com.example.paymentbe.model.Payment;
import com.example.paymentbe.model.PaymentStatus;
import com.example.paymentbe.model.Refund;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.paymentbe.model.PaymentMethod;

@DataJpaTest
class RefundRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // HAPPY PATH: Save and find refund by ID
    @Test
    void testSaveAndFindById() {
        // Arrange
        Payment payment = createTestPayment(PaymentStatus.PAID);
        Refund refund = Refund.builder()
                .payment(payment)
                .reason("Test refund reason")
                .build();

        // Act
        Refund savedRefund = entityManager.persistAndFlush(refund);
        Optional<Refund> foundRefund = refundRepository.findById(savedRefund.getId());

        // Assert
        assertThat(foundRefund).isPresent();
        assertThat(foundRefund.get().getId()).isEqualTo(savedRefund.getId());
        assertThat(foundRefund.get().getReason()).isEqualTo("Test refund reason");
    }

    // HAPPY PATH: Find refund by payment ID
    @Test
    void testFindByPaymentId() {
        // Arrange
        Payment payment = createTestPayment(PaymentStatus.PAID);
        Refund refund = Refund.builder()
                .payment(payment)
                .reason("Test refund")
                .build();
        entityManager.persistAndFlush(refund);

        // Act
        boolean exists = refundRepository.existsByPaymentId(payment.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    // UNHAPPY PATH: Find refund by non-existent payment ID
    @Test
    void testFindByNonExistentPaymentId() {
        // Act
        boolean exists = refundRepository.existsByPaymentId(UUID.randomUUID());

        // Assert
        assertThat(exists).isFalse();
    }

    // EDGE CASE: Create refund with empty reason
    @Test
    void testCreateRefundWithEmptyReason() {
        // Arrange
        Payment payment = createTestPayment(PaymentStatus.PAID);
        Refund refund = Refund.builder()
                .payment(payment)
                .reason("")
                .build();

        // Act
        Refund savedRefund = entityManager.persistAndFlush(refund);
        Optional<Refund> foundRefund = refundRepository.findById(savedRefund.getId());

        // Assert
        assertThat(foundRefund).isPresent();
        assertThat(foundRefund.get().getReason()).isEmpty();
    }

    // EDGE CASE: Create refund with very long reason
    @Test
    void testCreateRefundWithLongReason() {
        // Arrange
        Payment payment = createTestPayment(PaymentStatus.PAID);
        String longReason = "This is a very long refund reason that exceeds the normal length. "
                + "It should still be stored correctly in the database. "
                + "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                + "Nullam auctor, nisl eget ultricies tincidunt, nisl nisl aliquam nisl.";
        
        Refund refund = Refund.builder()
                .payment(payment)
                .reason(longReason)
                .build();

        // Act
        Refund savedRefund = entityManager.persistAndFlush(refund);
        Optional<Refund> foundRefund = refundRepository.findById(savedRefund.getId());

        // Assert
        assertThat(foundRefund).isPresent();
        assertThat(foundRefund.get().getReason()).hasSize(longReason.length());
    }

    // INTEGRATION TEST: Payment status changes when refund is created
    @Test
    void testPaymentStatusAfterRefund() {
        // Arrange
        Payment payment = createTestPayment(PaymentStatus.PAID);
        Refund refund = Refund.builder()
                .payment(payment)
                .reason("Test refund")
                .build();

        // Act
        entityManager.persistAndFlush(refund);
        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();

        // Assert
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    // Helper method to create test payments
    private Payment createTestPayment(PaymentStatus status) {
        Payment payment = Payment.builder()
                .userId(UUID.randomUUID().toString())
                .courseId(UUID.randomUUID().toString())
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(status)
                .paymentReference("PAY-" + UUID.randomUUID().toString().substring(0, 8))
                .build();
        return entityManager.persistAndFlush(payment);
    }
}