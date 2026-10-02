package com.ridelink.account_service.dto;

import com.ridelink.account_service.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull AccountStatus status
) {
}
