package com.example.paymentbe.controller;

import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final PaymentService paymentService;

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable String paymentId,
            @RequestParam String status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingPayments() {
        // Implementation would return list of pending payments
        return ResponseEntity.ok().build();
    }
}