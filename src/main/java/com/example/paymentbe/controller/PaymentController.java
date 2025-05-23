package com.example.paymentbe.controller; 
 
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; 

import com.example.paymentbe.dto.ErrorResponse;
import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundRequest;
import com.example.paymentbe.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@RestController 
@RequestMapping("/api/payments") // Ubah dari /api/payments menjadi /payments sesuai di Postman
@RequiredArgsConstructor 
public class PaymentController { 
    private final PaymentService paymentService; 
 
    @PostMapping 
    // Hapus anotasi @PreAuthorize("hasRole('STUDENT')")
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
    // Hapus anotasi @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> requestRefund( 
            @PathVariable String paymentId, 
            @Valid @RequestBody RefundRequest request) { 
        try { 
            return ResponseEntity.ok(paymentService.requestRefund(paymentId, request)); 
        } catch (Exception e) { 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) 
                    .body(new ErrorResponse(e.getMessage(), "REFUND_ERROR")); 
        } 
    } 
}