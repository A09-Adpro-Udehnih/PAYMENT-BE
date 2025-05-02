package com.example.paymentbe.dto;

import com.example.paymentbe.model.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Course ID is required")
    private String courseId;

    @NotBlank(message = "Course name is required")
    private String courseName;

    @NotBlank(message = "Tutor name is required")
    private String tutorName;

    @Positive(message = "Amount must be positive")
    private double amount;

    private PaymentMethod method;
}