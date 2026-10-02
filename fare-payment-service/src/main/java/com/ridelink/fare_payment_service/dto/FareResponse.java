package com.ridelink.fare_payment_service.dto;

public record FareResponse(
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
