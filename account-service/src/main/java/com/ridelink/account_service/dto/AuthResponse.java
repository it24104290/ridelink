package com.ridelink.account_service.dto;

import com.ridelink.account_service.domain.Role;

public record AuthResponse(
        String token,
        String tokenType,
        String accountId,
        String email,
        Role role
) {
}
