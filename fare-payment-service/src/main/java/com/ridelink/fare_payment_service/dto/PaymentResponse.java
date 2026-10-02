package com.ridelink.fare_payment_service.dto;

import com.ridelink.fare_payment_service.domain.PaymentStatus;

import java.time.Instant;

public record PaymentResponse(
        String paymentId,
        String rideId,
        String passengerAccountId,
        double amount,
        String currency,
        String method,
        PaymentStatus status,
        String receiptNumber,
        Instant createdAt
) {
}
