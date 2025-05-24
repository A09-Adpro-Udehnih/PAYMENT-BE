package com.example.paymentbe.controller;

import com.example.paymentbe.dto.*;
import com.example.paymentbe.service.PaymentService;
import com.example.paymentbe.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final RefundService refundService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            return ResponseEntity.ok(paymentService.processPayment(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "PAYMENT_ERROR"));
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable String paymentId) {
        try {
            return ResponseEntity.ok(paymentService.getPayment(paymentId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), "NOT_FOUND"));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(@PathVariable String userId) {
        return ResponseEntity.ok(paymentService.getUserPayments(userId));
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> requestRefund(
            @PathVariable String paymentId,
            @Valid @RequestBody RefundRequest request) {
        try {
            return ResponseEntity.ok(refundService.requestRefund(paymentId, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "REFUND_ERROR"));
        }
    }
}