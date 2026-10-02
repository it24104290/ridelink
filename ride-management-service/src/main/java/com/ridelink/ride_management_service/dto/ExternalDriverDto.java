package com.ridelink.ride_management_service.dto;

public record ExternalDriverDto(
        String id,
        String accountId,
        Object vehicle,
        String serviceArea,
        boolean available,
        LocationDto currentLocation
) {
}
