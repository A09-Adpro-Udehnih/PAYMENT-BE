package com.example.paymentbe.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundRequest;
import com.example.paymentbe.enums.*;
import com.example.paymentbe.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private PaymentRequest validRequest;
    private PaymentResponse successResponse;
    private UUID testPaymentId;
    private UUID testUserId;
    private UUID testCourseId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        testPaymentId = UUID.randomUUID();

        validRequest = new PaymentRequest();
        validRequest.setUserId(testUserId);
        validRequest.setCourseId(testCourseId);
        validRequest.setAmount(100.0);
        validRequest.setMethod(PaymentMethod.CREDIT_CARD);
        validRequest.setCardNumber("4111111111111111");
        validRequest.setCardCvc("123");

        successResponse = PaymentResponse.builder()
                .paymentId(testPaymentId.toString())
                .status(PaymentStatus.PAID)
                .paymentReference("PAY-123456")
                .build();
    }

    @Test
    void getPayment_Success() throws Exception {
        when(paymentService.getPayment(testPaymentId.toString())).thenReturn(successResponse);

        mockMvc.perform(get("/api/payments/{paymentId}", testPaymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(testPaymentId.toString()));

        verify(paymentService).getPayment(testPaymentId.toString());
    }

    @Test
    void getPayment_NotFound() throws Exception {
        when(paymentService.getPayment(anyString())).thenThrow(new RuntimeException("Payment not found"));

        mockMvc.perform(get("/api/payments/{paymentId}", "nonexistent"))
                .andExpect(status().isNotFound());

        verify(paymentService).getPayment("nonexistent");
    }

    @Test
    void requestRefund_Success() throws Exception {
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setReason("Test reason");

        PaymentResponse refundResponse = PaymentResponse.builder()
                .paymentId(testPaymentId.toString())
                .status(PaymentStatus.REFUNDED)
                .build();

        when(paymentService.requestRefund(eq(testPaymentId.toString()), any(RefundRequest.class)))
                .thenReturn(refundResponse);

        mockMvc.perform(post("/api/payments/{paymentId}/refund", testPaymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));

        verify(paymentService).requestRefund(eq(testPaymentId.toString()), any(RefundRequest.class));
    }

    @Test
    void requestRefund_InvalidPayment() throws Exception {
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setReason("Test reason");

        when(paymentService.requestRefund(anyString(), any(RefundRequest.class)))
                .thenThrow(new RuntimeException("Only PAID payments can be refunded"));

        mockMvc.perform(post("/api/payments/{paymentId}/refund", testPaymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentService).requestRefund(eq(testPaymentId.toString()), any(RefundRequest.class));
    }

    @Test
    void getUserPayments_Success() throws Exception {
        List<PaymentResponse> payments = Arrays.asList(
                PaymentResponse.builder().paymentId("1").build(),
                PaymentResponse.builder().paymentId("2").build()
        );

        when(paymentService.getUserPayments("user123")).thenReturn(payments);

        mockMvc.perform(get("/api/payments/user/{userId}", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(paymentService).getUserPayments("user123");
    }
}