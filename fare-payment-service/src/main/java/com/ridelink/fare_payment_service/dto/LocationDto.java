package com.ridelink.fare_payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocationDto(
        @NotBlank String placeName,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
