package com.ridelink.fare_payment_service.dto;

import com.ridelink.fare_payment_service.domain.PaymentStatus;

import java.time.Instant;

public record ReceiptResponse(
        String receiptNumber,
        String paymentId,
        String rideId,
        String passengerAccountId,
        double amount,
        String currency,
        String paymentMethod,
        PaymentStatus status,
        Instant issuedAt
) {
}
