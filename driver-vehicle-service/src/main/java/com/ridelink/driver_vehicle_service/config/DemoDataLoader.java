package com.ridelink.driver_vehicle_service.config;

import com.ridelink.driver_vehicle_service.domain.DriverProfile;
import com.ridelink.driver_vehicle_service.domain.GeoLocation;
import com.ridelink.driver_vehicle_service.domain.Vehicle;
import com.ridelink.driver_vehicle_service.repository.DriverProfileRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ConditionalOnProperty(name = "ridelink.seed-demo-data", havingValue = "true")
public class DemoDataLoader implements ApplicationRunner {

    private final DriverProfileRepository repository;

    public DemoDataLoader(DriverProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        DriverProfile profile = new DriverProfile();
        profile.setId("drv-001");
        profile.setAccountId("acc-driver-001");
        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Toyota");
        vehicle.setModel("Axio");
        vehicle.setColor("White");
        vehicle.setRegistrationNumber("ABC-1234");
        vehicle.setVehicleType("CAR");
        profile.setVehicle(vehicle);
        profile.setServiceArea("Colombo");
        profile.setAvailable(true);
        GeoLocation location = new GeoLocation();
        location.setPlaceName("Colombo Fort");
        location.setLatitude(6.9344);
        location.setLongitude(79.8428);
        profile.setCurrentLocation(location);
        profile.setUpdatedAt(Instant.now());
        repository.save(profile);
    }
}
