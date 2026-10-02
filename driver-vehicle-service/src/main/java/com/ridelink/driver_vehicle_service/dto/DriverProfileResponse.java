package com.ridelink.driver_vehicle_service.dto;

import com.ridelink.driver_vehicle_service.domain.GeoLocation;
import com.ridelink.driver_vehicle_service.domain.Vehicle;

import java.time.Instant;

public record DriverProfileResponse(
        String id,
        String accountId,
        Vehicle vehicle,
        String serviceArea,
        boolean available,
        GeoLocation currentLocation,
        Instant updatedAt
) {
}
