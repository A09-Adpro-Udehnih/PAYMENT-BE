package com.example.paymentbe.controller;

import com.example.paymentbe.dto.ErrorResponse;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.dto.RefundResponse;
import com.example.paymentbe.enums.PaymentStatus;
import com.example.paymentbe.enums.RefundStatus;
import com.example.paymentbe.service.PaymentService;
import com.example.paymentbe.service.RefundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private RefundService refundService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void updatePaymentStatus_Success() throws Exception {
        String paymentId = "payment123";
        String status = "PAID";
        PaymentResponse mockResponse = createMockPaymentResponse();
        
        when(paymentService.updatePaymentStatus(paymentId, status))
                .thenReturn(mockResponse);

        mockMvc.perform(put("/api/v1/payment/admin/{paymentId}/status", paymentId)
                        .param("status", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.paymentId").value(mockResponse.getPaymentId()))
                .andExpect(jsonPath("$.status").value(mockResponse.getStatus().toString()));

        verify(paymentService, times(1)).updatePaymentStatus(paymentId, status);
    }

    @Test
    void updatePaymentStatus_ThrowsException_ReturnsBadRequest() throws Exception {
        String paymentId = "payment123";
        String status = "INVALID_STATUS";
        String errorMessage = "Invalid payment status";
        
        when(paymentService.updatePaymentStatus(paymentId, status))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(put("/api/v1/payment/admin/{paymentId}/status", paymentId)
                        .param("status", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.errorType").value("STATUS_UPDATE_ERROR"));

        verify(paymentService, times(1)).updatePaymentStatus(paymentId, status);
    }

    @Test
    void getPendingPayments_Success() throws Exception {
        List<PaymentResponse> mockPayments = Arrays.asList(
                createMockPaymentResponse("payment1", PaymentStatus.PENDING),
                createMockPaymentResponse("payment2", PaymentStatus.PENDING)
        );
        
        when(paymentService.getPendingPayments()).thenReturn(mockPayments);

        mockMvc.perform(get("/api/v1/payment/admin/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].paymentId").value("payment1"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].paymentId").value("payment2"))
                .andExpect(jsonPath("$[1].status").value("PENDING"));

        verify(paymentService, times(1)).getPendingPayments();
    }

    @Test
    void getPendingRefunds_Success() throws Exception {
        List<RefundResponse> mockRefunds = Arrays.asList(
                createMockRefundResponse(UUID.randomUUID().toString(), RefundStatus.PENDING),
                createMockRefundResponse(UUID.randomUUID().toString(), RefundStatus.PENDING)
        );
        
        when(refundService.getPendingRefunds()).thenReturn(mockRefunds);

        mockMvc.perform(get("/api/v1/payment/admin/refunds/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(refundService, times(1)).getPendingRefunds();
    }

    @Test
    void processRefund_Success() throws Exception {
        String refundId = UUID.randomUUID().toString();
        String status = "ACCEPTED";
        String processedBy = "admin1";
        RefundResponse mockResponse = createMockRefundResponse(refundId, RefundStatus.ACCEPTED);
        mockResponse.setProcessedBy(processedBy);
        
        when(refundService.processRefund(refundId, status, processedBy))
                .thenReturn(mockResponse);

        mockMvc.perform(put("/api/v1/payment/admin/refunds/{refundId}/process", refundId)
                        .param("status", status)
                        .param("processedBy", processedBy)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(refundId))
                .andExpect(jsonPath("$.processedBy").value(processedBy));

        verify(refundService, times(1)).processRefund(refundId, status, processedBy);
    }

    @Test
    void processRefund_InvalidStatus_ReturnsBadRequest() throws Exception {
        String refundId = UUID.randomUUID().toString();
        String status = "INVALID_STATUS";
        String processedBy = "admin1";
        String errorMessage = "Invalid refund status";
        
        when(refundService.processRefund(refundId, status, processedBy))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(put("/api/v1/payment/admin/refunds/{refundId}/process", refundId)
                        .param("status", status)
                        .param("processedBy", processedBy)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.errorType").value("REFUND_PROCESS_ERROR"));

        verify(refundService, times(1)).processRefund(refundId, status, processedBy);
    }

    @Test
    void getRefund_Success() throws Exception {
        String refundId = UUID.randomUUID().toString();
        RefundResponse mockResponse = createMockRefundResponse(refundId, RefundStatus.PENDING);
        
        when(refundService.getRefund(refundId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/payment/admin/refunds/{refundId}", refundId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(refundId));

        verify(refundService, times(1)).getRefund(refundId);
    }

    @Test
    void getRefund_NotFound_ReturnsNotFound() throws Exception {
        String refundId = UUID.randomUUID().toString();
        String errorMessage = "Refund not found";
        
        when(refundService.getRefund(refundId))
                .thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/api/v1/payment/admin/refunds/{refundId}", refundId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.errorType").value("REFUND_NOT_FOUND"));

        verify(refundService, times(1)).getRefund(refundId);
    }

    private PaymentResponse createMockPaymentResponse() {
        return createMockPaymentResponse("payment123", PaymentStatus.PAID);
    }

    private PaymentResponse createMockPaymentResponse(String paymentId, PaymentStatus status) {
        return PaymentResponse.builder()
                .paymentId(paymentId)
                .userId(UUID.randomUUID())
                .courseId(UUID.randomUUID())
                .amount(100.0)
                .paymentMethod("CREDIT_CARD")
                .status(status)
                .paymentReference("PAY_REF_123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .refundReason(null)
                .build();
    }

    private RefundResponse createMockRefundResponse(String refundId, RefundStatus status) {
        return RefundResponse.builder()
                .id(UUID.fromString(refundId))
                .paymentId(UUID.randomUUID())
                .reason("Product not as described")
                .status(status)
                .createdAt(LocalDateTime.now())
                .requestedAt(LocalDateTime.now())
                .payment(createMockPaymentResponse())
                .build();
    }
}