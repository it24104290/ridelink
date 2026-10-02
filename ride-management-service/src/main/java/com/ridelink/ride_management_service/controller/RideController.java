package com.ridelink.ride_management_service.controller;

import com.ridelink.ride_management_service.dto.AssignDriverRequest;
import com.ridelink.ride_management_service.dto.CancelRideRequest;
import com.ridelink.ride_management_service.dto.CreateRideRequest;
import com.ridelink.ride_management_service.dto.PayRideRequest;
import com.ridelink.ride_management_service.dto.RideResponse;
import com.ridelink.ride_management_service.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@Tag(name = "Rides")
@SecurityRequirement(name = "bearerAuth")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Create a ride request with pickup, destination and automated driver matching")
    public RideResponse createRide(Authentication authentication, @Valid @RequestBody CreateRideRequest request) {
        return rideService.createRide(authentication.getName(), request);
    }

    @GetMapping("/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN', 'SERVICE')")
    @Operation(summary = "Get ride details by ride id")
    public RideResponse getRide(@PathVariable String rideId, Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return rideService.getRide(rideId, authentication.getName(), role);
    }

    @GetMapping("/passenger/me")
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Get current passenger's ride history")
    public List<RideResponse> myPassengerRides(Authentication authentication) {
        return rideService.getMyRidesAsPassenger(authentication.getName());
    }

    @GetMapping("/driver/me")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Get current driver's assigned rides")
    public List<RideResponse> myDriverRides(Authentication authentication) {
        return rideService.getMyRidesAsDriver(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all rides across the platform")
    public List<RideResponse> listAllRides() {
        return rideService.listAllRides();
    }

    @PatchMapping("/{rideId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @Operation(summary = "Assign a driver to a requested ride")
    public RideResponse assignDriver(
            @PathVariable String rideId,
            @Valid @RequestBody AssignDriverRequest request,
            Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return rideService.assignDriver(rideId, request, role);
    }

    @PatchMapping("/{rideId}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver accepts an assigned ride")
    public RideResponse acceptRide(@PathVariable String rideId, Authentication authentication) {
        return rideService.acceptRide(rideId, authentication.getName());
    }

    @PatchMapping("/{rideId}/start")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver starts the ride (pickup passenger)")
    public RideResponse startRide(@PathVariable String rideId, Authentication authentication) {
        return rideService.startRide(rideId, authentication.getName());
    }

    @PatchMapping("/{rideId}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver completes the ride (arrive at destination, calculates final fare)")
    public RideResponse completeRide(@PathVariable String rideId, Authentication authentication) {
        return rideService.completeRide(rideId, authentication.getName());
    }

    @PatchMapping("/{rideId}/cancel")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN')")
    @Operation(summary = "Cancel a ride and release driver")
    public RideResponse cancelRide(
            @PathVariable String rideId,
            @RequestBody(required = false) CancelRideRequest request,
            Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return rideService.cancelRide(rideId, authentication.getName(), role, request);
    }

    @PostMapping("/{rideId}/pay")
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Passenger pays for a completed ride and receives a receipt")
    public RideResponse payRide(
            @PathVariable String rideId,
            @Valid @RequestBody PayRideRequest request,
            Authentication authentication) {
        return rideService.payRide(rideId, authentication.getName(), request);
    }
}
