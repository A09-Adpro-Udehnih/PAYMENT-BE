package com.example.paymentbe.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.example.paymentbe.repository.PaymentRepository;
import com.example.paymentbe.repository.RefundRepository;
import com.example.paymentbe.service.PaymentService;
import com.example.paymentbe.service.RefundService;
import com.example.paymentbe.controller.PaymentController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtils.class})
@TestPropertySource(properties = {
    "jwt.secret=dWRlaG5paHNlY3JldHlhbmdwYW5qYW5nYmFuZ2V0eWFuZ3Rlcm55YXRhbWFzaWhrdXJhbmdwYW5qYW5n",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void corsConfigurationSource_AllowsConfiguredMethods() throws Exception {
        mockMvc.perform(options("/api/v1/payment")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"));
    }
} 