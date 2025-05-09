package com.example.paymentbe.repository;

import com.example.paymentbe.model.Payment;
import com.example.paymentbe.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByUserId(UUID userId);
    List<Payment> findByStatus(PaymentStatus status);
}