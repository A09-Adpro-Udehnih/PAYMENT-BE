package com.example.paymentbe.repository;

import com.example.paymentbe.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void shouldSaveAndFindPayment() {
        Payment payment = Payment.builder()
                .userId("user1")
                .amount(200.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .paymentReference("ref-abc")
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo("user1");
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void shouldFindAllPayments() {
        Payment p1 = Payment.builder()
                .userId("user1")
                .amount(100.0)
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.PAID)
                .paymentReference("ref-1")
                .createdAt(LocalDateTime.now())
                .build();

        Payment p2 = Payment.builder()
                .userId("user2")
                .amount(300.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.FAILED)
                .paymentReference("ref-2")
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(p1);
        paymentRepository.save(p2);

        List<Payment> payments = paymentRepository.findAll();
        assertThat(payments).hasSize(2);
    }

    @Test
    void shouldReturnEmptyForInvalidId() {
        UUID invalidId = UUID.randomUUID();
        var result = paymentRepository.findById(invalidId);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdatePaymentStatus() {
        Payment payment = Payment.builder()
                .userId("userX")
                .amount(150.0)
                .method(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.PENDING)
                .paymentReference("update-001")
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        saved.setStatus(PaymentStatus.PAID);
        Payment updated = paymentRepository.save(saved);

        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.PAID);
    }
}
