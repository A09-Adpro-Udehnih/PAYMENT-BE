package com.example.paymentbe.repository;

import com.example.paymentbe.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void shouldSaveAndFindPayment() {
        Payment payment = Payment.builder()
                .userId("user1")
                .courseId("course1")
                .courseName("Course 1")
                .tutorName("Tutor 1")
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PAID)
                .transactionDate(LocalDateTime.now())
                .paymentReference("REF123")
                .build();

        Payment saved = paymentRepository.save(payment);
        Payment found = paymentRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getUserId()).isEqualTo("user1");
        assertThat(found.getStatus()).isEqualTo(PaymentStatus.PAID);
    }
}