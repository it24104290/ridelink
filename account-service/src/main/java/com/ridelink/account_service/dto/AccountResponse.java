package com.ridelink.account_service.dto;

import com.ridelink.account_service.domain.AccountStatus;
import com.ridelink.account_service.domain.Role;

import java.time.Instant;

public record AccountResponse(
        String id,
        String fullName,
        String email,
        String phone,
        Role role,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
