package com.ridelink.ride_management_service.service;

import com.ridelink.ride_management_service.client.DriverClient;
import com.ridelink.ride_management_service.client.FareClient;
import com.ridelink.ride_management_service.domain.Location;
import com.ridelink.ride_management_service.domain.Ride;
import com.ridelink.ride_management_service.domain.RideStatus;
import com.ridelink.ride_management_service.dto.CreateRideRequest;
import com.ridelink.ride_management_service.dto.ExternalDriverDto;
import com.ridelink.ride_management_service.dto.ExternalFareDto;
import com.ridelink.ride_management_service.dto.LocationDto;
import com.ridelink.ride_management_service.exception.ApiException;
import com.ridelink.ride_management_service.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverClient driverServiceClient;

    @Mock
    private FareClient fareServiceClient;

    private RideService rideService;

    @BeforeEach
    void setUp() {
        rideService = new RideService(rideRepository, driverServiceClient, fareServiceClient);
    }

    @Test
    void createRideWithAutoAssignmentAssignsDriver() {
        LocationDto pickup = new LocationDto("Fort", 6.9344, 79.8428);
        LocationDto destination = new LocationDto("Kollupitiya", 6.9147, 79.8530);
        CreateRideRequest request = new CreateRideRequest(pickup, destination, "Colombo", true);

        ExternalFareDto fareDto = new ExternalFareDto("q-1", "r-1", 3.5, 8.0, 350.0, "LKR", "FARE-RULE-V1", false);
        when(fareServiceClient.estimateFare(any(), any(), any())).thenReturn(Optional.of(fareDto));

        ExternalDriverDto driverDto = new ExternalDriverDto("drv-1", "acc-drv-001", null, "Colombo", true, pickup);
        when(driverServiceClient.findEligibleDrivers(eq("Colombo"), anyDouble(), anyDouble()))
                .thenReturn(List.of(driverDto));

        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            if (r.getId() == null) r.setId("ride-001");
            return r;
        });

        var response = rideService.createRide("acc-passenger-001", request);

        assertNotNull(response);
        assertEquals(RideStatus.ASSIGNED, response.status());
        assertEquals("acc-drv-001", response.driverAccountId());
        assertEquals(350.0, response.estimatedFare());
        verify(driverServiceClient).updateDriverAvailability("acc-drv-001", false);
    }

    @Test
    void lifecycleTransitionsWorkCorrectly() {
        Ride ride = new Ride();
        ride.setId("ride-lifecycle");
        ride.setPassengerAccountId("acc-p-1");
        ride.setDriverAccountId("acc-d-1");
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setPickup(new Location("A", 6.9, 79.8));
        ride.setDestination(new Location("B", 6.91, 79.85));

        when(rideRepository.findById("ride-lifecycle")).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Accept
        var accepted = rideService.acceptRide("ride-lifecycle", "acc-d-1");
        assertEquals(RideStatus.ACCEPTED, accepted.status());

        // 2. Start
        var started = rideService.startRide("ride-lifecycle", "acc-d-1");
        assertEquals(RideStatus.IN_PROGRESS, started.status());

        // 3. Complete
        when(fareServiceClient.calculateFinalFare(anyString(), any(), any()))
                .thenReturn(Optional.of(new ExternalFareDto("q-2", "ride-lifecycle", 4.0, 10.0, 420.0, "LKR", "FARE-RULE-V1", true)));

        var completed = rideService.completeRide("ride-lifecycle", "acc-d-1");
        assertEquals(RideStatus.COMPLETED, completed.status());
        assertEquals(420.0, completed.finalFare());
        verify(driverServiceClient).updateDriverAvailability("acc-d-1", true);
    }

    @Test
    void invalidStatusTransitionThrowsBadRequest() {
        Ride ride = new Ride();
        ride.setId("ride-err");
        ride.setPassengerAccountId("acc-p-1");
        ride.setDriverAccountId("acc-d-1");
        ride.setStatus(RideStatus.REQUESTED);

        when(rideRepository.findById("ride-err")).thenReturn(Optional.of(ride));

        // Trying to start a ride while still REQUESTED should fail
        ApiException ex = assertThrows(ApiException.class, () -> rideService.startRide("ride-err", "acc-d-1"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void unassignedDriverCannotAcceptRide() {
        Ride ride = new Ride();
        ride.setId("ride-sec");
        ride.setPassengerAccountId("acc-p-1");
        ride.setDriverAccountId("acc-d-1");
        ride.setStatus(RideStatus.ASSIGNED);

        when(rideRepository.findById("ride-sec")).thenReturn(Optional.of(ride));

        ApiException ex = assertThrows(ApiException.class, () -> rideService.acceptRide("ride-sec", "acc-different-driver"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void cancelRideReleasesAssignedDriver() {
        Ride ride = new Ride();
        ride.setId("ride-cancel");
        ride.setPassengerAccountId("acc-p-1");
        ride.setDriverAccountId("acc-d-1");
        ride.setStatus(RideStatus.ASSIGNED);

        when(rideRepository.findById("ride-cancel")).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        var cancelled = rideService.cancelRide("ride-cancel", "acc-p-1", "PASSENGER", null);
        assertEquals(RideStatus.CANCELLED, cancelled.status());
        verify(driverServiceClient).updateDriverAvailability("acc-d-1", true);
    }
}
