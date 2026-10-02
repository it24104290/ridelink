package com.ridelink.ride_management_service.dto;

import com.ridelink.ride_management_service.domain.RideStatus;

import java.time.Instant;

public record RideResponse(
        String id,
        String passengerAccountId,
        String driverAccountId,
        LocationDto pickup,
        LocationDto destination,
        String serviceArea,
        RideStatus status,
        Double estimatedFare,
        Double finalFare,
        String currency,
        String paymentStatus,
        String paymentReceiptNumber,
        String cancellationReason,
        Instant requestedAt,
        Instant assignedAt,
        Instant acceptedAt,
        Instant startedAt,
        Instant completedAt,
        Instant cancelledAt,
        Instant updatedAt
) {
}
