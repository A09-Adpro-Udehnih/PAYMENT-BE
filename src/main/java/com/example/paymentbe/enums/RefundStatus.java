package com.example.paymentbe.enums;

public enum RefundStatus {
    PENDING("PENDING"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED");

    private final String status;

    RefundStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
} 