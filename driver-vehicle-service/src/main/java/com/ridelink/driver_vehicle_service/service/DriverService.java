package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.domain.DriverProfile;
import com.ridelink.driver_vehicle_service.domain.GeoLocation;
import com.ridelink.driver_vehicle_service.domain.Vehicle;
import com.ridelink.driver_vehicle_service.dto.AvailabilityRequest;
import com.ridelink.driver_vehicle_service.dto.DriverProfileResponse;
import com.ridelink.driver_vehicle_service.dto.LocationRequest;
import com.ridelink.driver_vehicle_service.dto.UpsertProfileRequest;
import com.ridelink.driver_vehicle_service.dto.VehicleRequest;
import com.ridelink.driver_vehicle_service.exception.ApiException;
import com.ridelink.driver_vehicle_service.repository.DriverProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class DriverService {

    private final DriverProfileRepository repository;
    private final DriverMatcher matcher = new DriverMatcher();

    public DriverService(DriverProfileRepository repository) {
        this.repository = repository;
    }

    public DriverProfileResponse upsertMine(String accountId, UpsertProfileRequest request) {
        DriverProfile profile = repository.findByAccountId(accountId).orElseGet(DriverProfile::new);
        profile.setAccountId(accountId);
        profile.setVehicle(toVehicle(request.vehicle()));
        profile.setServiceArea(request.serviceArea().trim());
        profile.setAvailable(request.available());
        profile.setCurrentLocation(toLocation(request.currentLocation()));
        profile.setUpdatedAt(Instant.now());
        return toResponse(repository.save(profile));
    }

    public DriverProfileResponse getMine(String accountId) {
        return toResponse(findByAccount(accountId));
    }

    public DriverProfileResponse getByAccountId(String accountId) {
        return toResponse(findByAccount(accountId));
    }

    public DriverProfileResponse updateAvailability(String accountId, AvailabilityRequest request) {
        DriverProfile profile = findByAccount(accountId);
        profile.setAvailable(request.available());
        profile.setUpdatedAt(Instant.now());
        return toResponse(repository.save(profile));
    }

    public DriverProfileResponse updateLocation(String accountId, LocationRequest request) {
        DriverProfile profile = findByAccount(accountId);
        profile.setCurrentLocation(toLocation(request));
        profile.setUpdatedAt(Instant.now());
        return toResponse(repository.save(profile));
    }

    public DriverProfileResponse updateVehicle(String accountId, VehicleRequest request) {
        DriverProfile profile = findByAccount(accountId);
        profile.setVehicle(toVehicle(request));
        profile.setUpdatedAt(Instant.now());
        return toResponse(repository.save(profile));
    }

    public Vehicle getVehicle(String accountId) {
        return findByAccount(accountId).getVehicle();
    }

    public DriverProfileResponse updateAvailabilityForAccount(String accountId, AvailabilityRequest request) {
        DriverProfile profile = findByAccount(accountId);
        profile.setAvailable(request.available());
        profile.setUpdatedAt(Instant.now());
        return toResponse(repository.save(profile));
    }

    public List<DriverProfileResponse> findEligible(String serviceArea, Double latitude, Double longitude) {
        List<DriverProfile> candidates = serviceArea == null || serviceArea.isBlank()
                ? repository.findByAvailableTrue()
                : repository.findByAvailableTrueAndServiceAreaIgnoreCase(serviceArea.trim());
        if (latitude == null || longitude == null) {
            return candidates.stream().map(this::toResponse).toList();
        }
        return candidates.stream()
                .sorted(Comparator.comparingDouble(driver -> matcher.distanceKm(
                        latitude, longitude,
                        driver.getCurrentLocation().getLatitude(),
                        driver.getCurrentLocation().getLongitude())))
                .map(this::toResponse)
                .toList();
    }

    private DriverProfile findByAccount(String accountId) {
        return repository.findByAccountId(accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver profile not found"));
    }

    private Vehicle toVehicle(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setColor(request.color());
        vehicle.setRegistrationNumber(request.registrationNumber());
        vehicle.setVehicleType(request.vehicleType());
        return vehicle;
    }

    private GeoLocation toLocation(LocationRequest request) {
        GeoLocation location = new GeoLocation();
        location.setPlaceName(request.placeName());
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        return location;
    }

    private DriverProfileResponse toResponse(DriverProfile profile) {
        return new DriverProfileResponse(
                profile.getId(),
                profile.getAccountId(),
                profile.getVehicle(),
                profile.getServiceArea(),
                profile.isAvailable(),
                profile.getCurrentLocation(),
                profile.getUpdatedAt());
    }
}
