package com.ridelink.fare_payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(
        @NotBlank String rideId,
        @Positive double amount,
        @NotBlank String method,
        String currency
) {
}
