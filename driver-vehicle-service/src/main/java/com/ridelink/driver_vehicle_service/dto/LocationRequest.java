package com.ridelink.driver_vehicle_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocationRequest(
        @NotBlank String placeName,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
