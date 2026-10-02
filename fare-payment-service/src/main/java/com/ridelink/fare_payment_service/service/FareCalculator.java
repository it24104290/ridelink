package com.ridelink.fare_payment_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Documented fare rule (v1):
 * amount = BASE + (distanceKm * PER_KM) + (durationMin * PER_MINUTE)
 * durationMin is estimated as (distanceKm / averageSpeedKmh) * 60
 * Values are rounded to two decimal places. Currency is LKR.
 */
@Component
public class FareCalculator {

    public static final String RULE_VERSION = "FARE-RULE-V1";

    private final double baseAmount;
    private final double perKm;
    private final double perMinute;
    private final double averageSpeedKmh;

    public FareCalculator(
            @Value("${ridelink.fare.base-amount}") double baseAmount,
            @Value("${ridelink.fare.per-km}") double perKm,
            @Value("${ridelink.fare.per-minute}") double perMinute,
            @Value("${ridelink.fare.average-speed-kmh}") double averageSpeedKmh) {
        this.baseAmount = baseAmount;
        this.perKm = perKm;
        this.perMinute = perMinute;
        this.averageSpeedKmh = averageSpeedKmh;
    }

    public double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return round(earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    public double durationMin(double distanceKm) {
        return round((distanceKm / averageSpeedKmh) * 60.0);
    }

    public double calculate(double distanceKm, double durationMin) {
        return round(baseAmount + (distanceKm * perKm) + (durationMin * perMinute));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
