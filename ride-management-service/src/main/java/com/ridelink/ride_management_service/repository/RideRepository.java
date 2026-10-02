package com.ridelink.ride_management_service.repository;

import com.ridelink.ride_management_service.domain.Ride;
import com.ridelink.ride_management_service.domain.RideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RideRepository extends MongoRepository<Ride, String> {

    List<Ride> findByPassengerAccountIdOrderByRequestedAtDesc(String passengerAccountId);

    List<Ride> findByDriverAccountIdOrderByRequestedAtDesc(String driverAccountId);

    List<Ride> findByStatus(RideStatus status);
}
