package com.ridelink.fare_payment_service.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FareCalculatorTest {

    private final FareCalculator calculator =
            new FareCalculator(150.0, 80.0, 5.0, 30.0);

    @Test
    void tenKilometresCosts1050WithEstimatedDuration() {
        double duration = calculator.durationMin(10.0);
        double fare = calculator.calculate(10.0, duration);

        assertEquals(20.0, duration, 0.001);
        assertEquals(1050.0, fare, 0.001);
    }

    @Test
    void zeroDistanceAndDurationCostsBaseFareOnly() {
        double fare = calculator.calculate(0.0, 0.0);

        assertEquals(150.0, fare, 0.001);
    }
}