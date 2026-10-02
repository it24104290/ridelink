package com.ridelink.driver_vehicle_service.repository;

import com.ridelink.driver_vehicle_service.domain.DriverProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DriverProfileRepository extends MongoRepository<DriverProfile, String> {

    Optional<DriverProfile> findByAccountId(String accountId);

    List<DriverProfile> findByAvailableTrueAndServiceAreaIgnoreCase(String serviceArea);

    List<DriverProfile> findByAvailableTrue();
}
