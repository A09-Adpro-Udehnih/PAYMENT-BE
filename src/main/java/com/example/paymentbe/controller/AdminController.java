package com.example.paymentbe.controller;

import com.example.paymentbe.dto.*;
import com.example.paymentbe.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment/admin")
@RequiredArgsConstructor
// @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final PaymentService paymentService;

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable String paymentId,
            @RequestParam String status) {
        try {
            return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "STATUS_UPDATE_ERROR"));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PaymentResponse>> getPendingPayments() {
        return ResponseEntity.ok(paymentService.getPendingPayments());
    }
}