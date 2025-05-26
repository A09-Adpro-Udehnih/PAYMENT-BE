package com.example.paymentbe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundRequest {
    @NotBlank(message = "Reason is required")
    private String reason;
}