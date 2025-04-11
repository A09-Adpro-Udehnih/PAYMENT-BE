package com.example.paymentbe.controller;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.model.PaymentMethod;
import com.example.paymentbe.model.PaymentStatus;
import com.example.paymentbe.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreatePaymentreturnsSuccess() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setUserId("user123");
        request.setAmount(150.0);
        request.setMethod(PaymentMethod.CREDIT_CARD);

        PaymentResponse mockResponse = PaymentResponse.builder()
                .paymentId(UUID.randomUUID().toString())
                .status(PaymentStatus.PAID)
                .message("Payment successful")
                .build();

        Mockito.when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.message").value("Payment successful"));
    }

    @Test
    void testCreatePaymentreturnsFailed() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setUserId("user456");
        request.setAmount(999.0);
        request.setMethod(PaymentMethod.BANK_TRANSFER);

        PaymentResponse mockResponse = PaymentResponse.builder()
                .paymentId(UUID.randomUUID().toString())
                .status(PaymentStatus.FAILED)
                .message("Payment failed")
                .build();

        Mockito.when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Payment failed"));
    }

    @Test
    void testCreatePaymentwithInvalidAmount_returnsBadRequest() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setUserId("user789");
        request.setAmount(-50.0); // Invalid negative amount
        request.setMethod(PaymentMethod.CREDIT_CARD);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreatePaymentwithMissingUserId_returnsBadRequest() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setUserId(""); // Missing userId
        request.setAmount(100.0);
        request.setMethod(PaymentMethod.CREDIT_CARD);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
