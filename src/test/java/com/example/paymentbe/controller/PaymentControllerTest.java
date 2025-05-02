package com.example.paymentbe.controller;

import com.example.paymentbe.dto.PaymentRequest;
import com.example.paymentbe.dto.PaymentResponse;
import com.example.paymentbe.model.PaymentMethod;
import com.example.paymentbe.model.PaymentStatus;
import com.example.paymentbe.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPayment_ShouldReturnCreated() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setUserId("user1");
        request.setCourseId("course1");
        request.setCourseName("Course 1");
        request.setTutorName("Tutor 1");
        request.setAmount(100.0);
        request.setMethod(PaymentMethod.CREDIT_CARD);

        PaymentResponse response = PaymentResponse.builder()
                .paymentId("123")
                .status(PaymentStatus.PAID)
                .message("Payment successful")
                .build();

        when(paymentService.processPayment(any())).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.message").value("Payment successful"));
    }
}