package com.ridelink.ride_management_service.dto;

import jakarta.validation.constraints.NotBlank;

public record PayRideRequest(
        @NotBlank String paymentMethod
) {
}
