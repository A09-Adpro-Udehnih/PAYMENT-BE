package com.example.paymentbe.repository;

import com.example.paymentbe.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {
    boolean existsByPaymentId(UUID paymentId);
    List<Refund> findByPaymentId(UUID paymentId);
}