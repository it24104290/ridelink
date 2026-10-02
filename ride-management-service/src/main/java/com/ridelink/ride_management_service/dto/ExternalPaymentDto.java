package com.ridelink.ride_management_service.dto;

import java.time.Instant;

public record ExternalPaymentDto(
        String paymentId,
        String rideId,
        String passengerAccountId,
        double amount,
        String currency,
        String method,
        String status,
        String receiptNumber,
        Instant createdAt
) {
}
