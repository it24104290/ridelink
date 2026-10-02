package com.ridelink.driver_vehicle_service.controller;

import com.ridelink.driver_vehicle_service.dto.AvailabilityRequest;
import com.ridelink.driver_vehicle_service.dto.DriverProfileResponse;
import com.ridelink.driver_vehicle_service.dto.LocationRequest;
import com.ridelink.driver_vehicle_service.dto.UpsertProfileRequest;
import com.ridelink.driver_vehicle_service.service.DriverService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Drivers")
@SecurityRequirement(name = "bearerAuth")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Create or update the authenticated driver's operational profile")
    public DriverProfileResponse upsertMine(Authentication authentication, @Valid @RequestBody UpsertProfileRequest request) {
        return driverService.upsertMine(authentication.getName(), request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "View the authenticated driver's profile")
    public DriverProfileResponse me(Authentication authentication) {
        return driverService.getMine(authentication.getName());
    }

    @PatchMapping("/me/availability")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Update availability")
    public DriverProfileResponse availability(Authentication authentication, @Valid @RequestBody AvailabilityRequest request) {
        return driverService.updateAvailability(authentication.getName(), request);
    }

    @PatchMapping("/me/location")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Update simulated current location")
    public DriverProfileResponse location(Authentication authentication, @Valid @RequestBody LocationRequest request) {
        return driverService.updateLocation(authentication.getName(), request);
    }

    @PutMapping("/me/vehicle")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Update driver vehicle details")
    public DriverProfileResponse updateVehicle(Authentication authentication, @Valid @RequestBody com.ridelink.driver_vehicle_service.dto.VehicleRequest request) {
        return driverService.updateVehicle(authentication.getName(), request);
    }

    @GetMapping("/me/vehicle")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "View driver vehicle details")
    public com.ridelink.driver_vehicle_service.domain.Vehicle getVehicle(Authentication authentication) {
        return driverService.getVehicle(authentication.getName());
    }

    @PatchMapping("/{accountId}/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @Operation(summary = "Update driver availability by account id (Interservice / Admin)")
    public DriverProfileResponse updateAvailabilityByAccountId(
            @PathVariable String accountId,
            @Valid @RequestBody AvailabilityRequest request) {
        return driverService.updateAvailabilityForAccount(accountId, request);
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN', 'SERVICE')")
    @Operation(summary = "Retrieve eligible available drivers")
    public List<DriverProfileResponse> available(
            @RequestParam(required = false) String serviceArea,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        return driverService.findEligible(serviceArea, latitude, longitude);
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE', 'PASSENGER', 'DRIVER')")
    @Operation(summary = "Get a driver profile by account id")
    public DriverProfileResponse byAccount(@PathVariable String accountId) {
        return driverService.getByAccountId(accountId);
    }
}
