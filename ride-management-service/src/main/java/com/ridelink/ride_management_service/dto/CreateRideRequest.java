package com.ridelink.ride_management_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRideRequest(
        @Valid @NotNull LocationDto pickup,
        @Valid @NotNull LocationDto destination,
        @NotBlank String serviceArea,
        Boolean autoAssignDriver
) {
}
