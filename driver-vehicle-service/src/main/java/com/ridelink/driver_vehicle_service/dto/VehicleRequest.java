package com.ridelink.driver_vehicle_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleRequest(
        @NotBlank String make,
        @NotBlank String model,
        @NotBlank String color,
        @NotBlank String registrationNumber,
        @NotBlank String vehicleType
) {
}
