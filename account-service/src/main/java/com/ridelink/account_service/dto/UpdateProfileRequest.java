package com.ridelink.account_service.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String fullName,
        @NotBlank String phone
) {
}
