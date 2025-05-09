package com.example.paymentbe.repository;

import com.example.paymentbe.model.Payment;
import com.example.paymentbe.model.PaymentMethod;
import com.example.paymentbe.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    public void testSaveAndFindById() {
        // Setup
        Payment payment = createTestPayment();
        
        // Execute
        Payment savedPayment = paymentRepository.save(payment);
        Optional<Payment> foundPayment = paymentRepository.findById(savedPayment.getId());
        
        // Verify
        assertTrue(foundPayment.isPresent());
        assertEquals(payment.getUserId(), foundPayment.get().getUserId());
        assertEquals(payment.getCourseId(), foundPayment.get().getCourseId());
        assertEquals(payment.getAmount(), foundPayment.get().getAmount());
    }
    
    @Test
    public void testFindByStatus() {
        // Setup
        Payment payment1 = createTestPayment();
        payment1.setStatus(PaymentStatus.PENDING);
        
        Payment payment2 = createTestPayment();
        payment2.setStatus(PaymentStatus.PAID);
        
        paymentRepository.save(payment1);
        paymentRepository.save(payment2);
        
        // Execute
        List<Payment> pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);
        List<Payment> paidPayments = paymentRepository.findByStatus(PaymentStatus.PAID);
        
        // Verify
        assertEquals(1, pendingPayments.size());
        assertEquals(1, paidPayments.size());
        assertEquals(PaymentStatus.PENDING, pendingPayments.get(0).getStatus());
        assertEquals(PaymentStatus.PAID, paidPayments.get(0).getStatus());
    }
    
    @Test
    public void testFindByUserId() {
        // Setup
        Payment payment1 = createTestPayment();
        payment1.setUserId("user1");
        
        Payment payment2 = createTestPayment();
        payment2.setUserId("user2");
        
        paymentRepository.save(payment1);
        paymentRepository.save(payment2);
        
        // Execute
        List<Payment> user1Payments = paymentRepository.findByUserId("user1");
        List<Payment> user2Payments = paymentRepository.findByUserId("user2");
        
        // Verify
        assertEquals(1, user1Payments.size());
        assertEquals(1, user2Payments.size());
        assertEquals("user1", user1Payments.get(0).getUserId());
        assertEquals("user2", user2Payments.get(0).getUserId());
    }
    
    @Test
    public void testUpdatePayment() {
        // Setup
        Payment payment = createTestPayment();
        payment = paymentRepository.save(payment);
        
        // Execute
        payment.setStatus(PaymentStatus.PAID);
        Payment updatedPayment = paymentRepository.save(payment);
        Optional<Payment> foundPayment = paymentRepository.findById(payment.getId());
        
        // Verify
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertTrue(foundPayment.isPresent());
        assertEquals(PaymentStatus.PAID, foundPayment.get().getStatus());
    }
    
    @Test
    public void testFindNonExistentPayment() {
        // Execute
        Optional<Payment> payment = paymentRepository.findById(UUID.randomUUID());
        
        // Verify
        assertFalse(payment.isPresent());
    }
    
    @Test
    public void testFindByNonExistentUserId() {
        // Execute
        List<Payment> payments = paymentRepository.findByUserId("nonexistent");
        
        // Verify
        assertTrue(payments.isEmpty());
    }
    
    @Test
    public void testFindByEmptyUserId() {
        // This test depends on your validation logic
        // Execute
        List<Payment> payments = paymentRepository.findByUserId("");
        
        // Verify
        assertTrue(payments.isEmpty());
    }
    
    @Test
    void testFindByNullStatus_returnsEmpty() {
        var result = paymentRepository.findByStatus(null);
        assertThat(result).isEmpty();
    }    
    
    private Payment createTestPayment() {
        return Payment.builder()
                .userId("testUser")
                .courseId("testCourse")
                .amount(100.0)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .cardLastFour("1234")
                .paymentReference("TEST-REF")
                .createdAt(LocalDateTime.now())
                .build();
    }
}