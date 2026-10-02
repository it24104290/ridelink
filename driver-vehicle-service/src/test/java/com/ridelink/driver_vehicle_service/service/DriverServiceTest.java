package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.domain.DriverProfile;
import com.ridelink.driver_vehicle_service.domain.GeoLocation;
import com.ridelink.driver_vehicle_service.repository.DriverProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverProfileRepository repository;

    private DriverService driverService;

    @BeforeEach
    void setUp() {
        driverService = new DriverService(repository);
    }

    @Test
    void nearestEligibleDriverIsReturnedFirst() {
        DriverProfile closer = profile("near", 6.93, 79.84);
        DriverProfile farther = profile("far", 7.29, 80.63);
        when(repository.findByAvailableTrueAndServiceAreaIgnoreCase("Colombo"))
                .thenReturn(List.of(farther, closer));

        var result = driverService.findEligible("Colombo", 6.9344, 79.8428);

        assertEquals(2, result.size());
        assertEquals("near", result.get(0).accountId());
    }

    @Test
    void matcherComputesPositiveDistance() {
        DriverMatcher matcher = new DriverMatcher();
        double km = matcher.distanceKm(6.9271, 79.8612, 6.9147, 79.9730);
        org.junit.jupiter.api.Assertions.assertTrue(km > 5 && km < 20);
    }

    @Test
    void updateVehicleUpdatesProfileVehicle() {
        DriverProfile profile = profile("drv-test", 6.9, 79.8);
        when(repository.findByAccountId("drv-test")).thenReturn(java.util.Optional.of(profile));
        when(repository.save(org.mockito.ArgumentMatchers.any(DriverProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new com.ridelink.driver_vehicle_service.dto.VehicleRequest("Honda", "Civic", "Black", "WP-CAB-1234", "SEDAN");
        var res = driverService.updateVehicle("drv-test", req);

        assertEquals("Honda", res.vehicle().getMake());
        assertEquals("Civic", res.vehicle().getModel());
    }

    @Test
    void updateAvailabilityForAccountUpdatesStatus() {
        DriverProfile profile = profile("drv-test", 6.9, 79.8);
        when(repository.findByAccountId("drv-test")).thenReturn(java.util.Optional.of(profile));
        when(repository.save(org.mockito.ArgumentMatchers.any(DriverProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = driverService.updateAvailabilityForAccount("drv-test", new com.ridelink.driver_vehicle_service.dto.AvailabilityRequest(false));
        org.junit.jupiter.api.Assertions.assertFalse(res.available());
    }

    private DriverProfile profile(String accountId, double lat, double lng) {
        DriverProfile profile = new DriverProfile();
        profile.setAccountId(accountId);
        profile.setAvailable(true);
        profile.setServiceArea("Colombo");
        GeoLocation location = new GeoLocation();
        location.setLatitude(lat);
        location.setLongitude(lng);
        profile.setCurrentLocation(location);
        return profile;
    }
}
