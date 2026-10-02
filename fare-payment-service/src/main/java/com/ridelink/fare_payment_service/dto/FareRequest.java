package com.ridelink.fare_payment_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record FareRequest(
        String rideId,
        @Valid @NotNull LocationDto pickup,
        @Valid @NotNull LocationDto destination
) {
}
