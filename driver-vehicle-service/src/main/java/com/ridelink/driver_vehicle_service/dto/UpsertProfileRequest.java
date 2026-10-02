package com.ridelink.driver_vehicle_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpsertProfileRequest(
        @Valid @NotNull VehicleRequest vehicle,
        @NotBlank String serviceArea,
        @NotNull Boolean available,
        @Valid @NotNull LocationRequest currentLocation
) {
}
