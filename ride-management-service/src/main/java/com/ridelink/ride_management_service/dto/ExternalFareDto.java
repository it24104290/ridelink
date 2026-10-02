package com.ridelink.ride_management_service.dto;

public record ExternalFareDto(
        String quoteId,
        String rideId,
        double distanceKm,
        double durationMin,
        double amount,
        String currency,
        String ruleVersion,
        boolean finalFare
) {
}
