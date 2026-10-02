package com.ridelink.ride_management_service.client;

import com.ridelink.ride_management_service.dto.ExternalDriverDto;

import java.util.List;

public interface DriverClient {

    List<ExternalDriverDto> findEligibleDrivers(String serviceArea, double latitude, double longitude);

    boolean updateDriverAvailability(String driverAccountId, boolean available);
}
