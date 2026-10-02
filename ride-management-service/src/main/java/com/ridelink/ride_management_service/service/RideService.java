package com.ridelink.ride_management_service.service;

import com.ridelink.ride_management_service.client.DriverClient;
import com.ridelink.ride_management_service.client.FareClient;
import com.ridelink.ride_management_service.client.DriverServiceClient;
import com.ridelink.ride_management_service.client.FareServiceClient;
import com.ridelink.ride_management_service.domain.Location;
import com.ridelink.ride_management_service.domain.Ride;
import com.ridelink.ride_management_service.domain.RideStatus;
import com.ridelink.ride_management_service.dto.AssignDriverRequest;
import com.ridelink.ride_management_service.dto.CancelRideRequest;
import com.ridelink.ride_management_service.dto.CreateRideRequest;
import com.ridelink.ride_management_service.dto.ExternalDriverDto;
import com.ridelink.ride_management_service.dto.ExternalFareDto;
import com.ridelink.ride_management_service.dto.ExternalPaymentDto;
import com.ridelink.ride_management_service.dto.LocationDto;
import com.ridelink.ride_management_service.dto.PayRideRequest;
import com.ridelink.ride_management_service.dto.RideResponse;
import com.ridelink.ride_management_service.exception.ApiException;
import com.ridelink.ride_management_service.repository.RideRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final DriverClient driverServiceClient;
    private final FareClient fareServiceClient;

    public RideService(
            RideRepository rideRepository,
            DriverClient driverServiceClient,
            FareClient fareServiceClient) {
        this.rideRepository = rideRepository;
        this.driverServiceClient = driverServiceClient;
        this.fareServiceClient = fareServiceClient;
    }

    public RideResponse createRide(String passengerAccountId, CreateRideRequest request) {
        Instant now = Instant.now();
        Ride ride = new Ride();
        ride.setPassengerAccountId(passengerAccountId);
        ride.setPickup(toLocation(request.pickup()));
        ride.setDestination(toLocation(request.destination()));
        ride.setServiceArea(request.serviceArea().trim());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setCurrency("LKR");
        ride.setPaymentStatus("UNPAID");
        ride.setRequestedAt(now);
        ride.setUpdatedAt(now);

        Ride saved = rideRepository.save(ride);

        // Interservice 1: Request fare estimation from Fare & Payment Service
        Optional<ExternalFareDto> fareOpt = fareServiceClient.estimateFare(
                saved.getId(), request.pickup(), request.destination());
        if (fareOpt.isPresent()) {
            saved.setEstimatedFare(fareOpt.get().amount());
            saved.setCurrency(fareOpt.get().currency());
        }

        // Interservice 2: Auto-assign closest eligible driver if requested
        boolean autoAssign = request.autoAssignDriver() == null || Boolean.TRUE.equals(request.autoAssignDriver());
        if (autoAssign) {
            List<ExternalDriverDto> eligibleDrivers = driverServiceClient.findEligibleDrivers(
                    request.serviceArea(),
                    request.pickup().latitude(),
                    request.pickup().longitude());

            if (!eligibleDrivers.isEmpty()) {
                ExternalDriverDto chosenDriver = eligibleDrivers.get(0);
                saved.setDriverAccountId(chosenDriver.accountId());
                saved.setStatus(RideStatus.ASSIGNED);
                saved.setAssignedAt(now);
                driverServiceClient.updateDriverAvailability(chosenDriver.accountId(), false);
            }
        }

        return toResponse(rideRepository.save(saved));
    }

    public RideResponse assignDriver(String rideId, AssignDriverRequest request, String callerRole) {
        Ride ride = findRide(rideId);
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot assign driver to ride in status: " + ride.getStatus());
        }

        ride.setDriverAccountId(request.driverAccountId().trim());
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(Instant.now());
        ride.setUpdatedAt(Instant.now());

        driverServiceClient.updateDriverAvailability(request.driverAccountId().trim(), false);
        return toResponse(rideRepository.save(ride));
    }

    public RideResponse acceptRide(String rideId, String driverAccountId) {
        Ride ride = findRide(rideId);
        if (ride.getDriverAccountId() != null && !ride.getDriverAccountId().equals(driverAccountId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Driver is not assigned to this ride");
        }
        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot accept ride in status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(Instant.now());
        ride.setUpdatedAt(Instant.now());
        return toResponse(rideRepository.save(ride));
    }

    public RideResponse startRide(String rideId, String driverAccountId) {
        Ride ride = findRide(rideId);
        if (ride.getDriverAccountId() != null && !ride.getDriverAccountId().equals(driverAccountId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Driver is not assigned to this ride");
        }
        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot start ride in status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(Instant.now());
        ride.setUpdatedAt(Instant.now());
        return toResponse(rideRepository.save(ride));
    }

    public RideResponse completeRide(String rideId, String driverAccountId) {
        Ride ride = findRide(rideId);
        if (ride.getDriverAccountId() != null && !ride.getDriverAccountId().equals(driverAccountId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Driver is not assigned to this ride");
        }
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot complete ride in status: " + ride.getStatus());
        }

        Instant now = Instant.now();
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(now);
        ride.setUpdatedAt(now);

        // Interservice 3: Final fare calculation from Fare Service
        LocationDto pickupDto = toLocationDto(ride.getPickup());
        LocationDto destDto = toLocationDto(ride.getDestination());
        Optional<ExternalFareDto> finalFareOpt = fareServiceClient.calculateFinalFare(rideId, pickupDto, destDto);
        if (finalFareOpt.isPresent()) {
            ride.setFinalFare(finalFareOpt.get().amount());
            ride.setCurrency(finalFareOpt.get().currency());
        } else if (ride.getEstimatedFare() != null) {
            ride.setFinalFare(ride.getEstimatedFare());
        } else {
            ride.setFinalFare(200.0);
        }

        // Release driver back to available
        driverServiceClient.updateDriverAvailability(driverAccountId, true);

        return toResponse(rideRepository.save(ride));
    }

    public RideResponse cancelRide(String rideId, String callerId, String callerRole, CancelRideRequest request) {
        Ride ride = findRide(rideId);

        if (!"ADMIN".equals(callerRole) && !callerId.equals(ride.getPassengerAccountId()) && !callerId.equals(ride.getDriverAccountId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: cannot cancel this ride");
        }

        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot cancel ride in status: " + ride.getStatus());
        }

        // Free driver if one was assigned
        if (ride.getDriverAccountId() != null) {
            driverServiceClient.updateDriverAvailability(ride.getDriverAccountId(), true);
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancellationReason(request != null && request.reason() != null && !request.reason().isBlank()
                ? request.reason()
                : "Cancelled by " + callerRole);
        ride.setCancelledAt(Instant.now());
        ride.setUpdatedAt(Instant.now());

        return toResponse(rideRepository.save(ride));
    }

    public RideResponse payRide(String rideId, String passengerAccountId, PayRideRequest request) {
        Ride ride = findRide(rideId);
        if (!passengerAccountId.equals(ride.getPassengerAccountId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the requesting passenger can pay for this ride");
        }
        if (ride.getStatus() != RideStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Can only pay for a completed ride. Current status: " + ride.getStatus());
        }
        if ("PAID".equalsIgnoreCase(ride.getPaymentStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ride is already paid");
        }

        double amountToPay = ride.getFinalFare() != null
                ? ride.getFinalFare()
                : (ride.getEstimatedFare() != null ? ride.getEstimatedFare() : 200.0);

        // Interservice 4: Simulated payment recording in Fare & Payment Service
        Optional<ExternalPaymentDto> paymentOpt = fareServiceClient.recordPayment(
                passengerAccountId, rideId, amountToPay, request.paymentMethod(), ride.getCurrency());

        if (paymentOpt.isPresent() && "COMPLETED".equalsIgnoreCase(paymentOpt.get().status())) {
            ride.setPaymentStatus("PAID");
            ride.setPaymentReceiptNumber(paymentOpt.get().receiptNumber());
            ride.setUpdatedAt(Instant.now());
            return toResponse(rideRepository.save(ride));
        } else {
            ride.setPaymentStatus("PAYMENT_FAILED");
            ride.setUpdatedAt(Instant.now());
            rideRepository.save(ride);
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Simulated payment failed with method: " + request.paymentMethod());
        }
    }

    public RideResponse getRide(String rideId, String callerId, String callerRole) {
        Ride ride = findRide(rideId);
        if (!"ADMIN".equals(callerRole) && !"SERVICE".equals(callerRole)
                && !callerId.equals(ride.getPassengerAccountId())
                && !callerId.equals(ride.getDriverAccountId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied to ride details");
        }
        return toResponse(ride);
    }

    public List<RideResponse> getMyRidesAsPassenger(String passengerAccountId) {
        return rideRepository.findByPassengerAccountIdOrderByRequestedAtDesc(passengerAccountId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RideResponse> getMyRidesAsDriver(String driverAccountId) {
        return rideRepository.findByDriverAccountIdOrderByRequestedAtDesc(driverAccountId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RideResponse> listAllRides() {
        return rideRepository.findAll().stream().map(this::toResponse).toList();
    }

    private Ride findRide(String rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ride not found: " + rideId));
    }

    private Location toLocation(LocationDto dto) {
        return new Location(dto.placeName(), dto.latitude(), dto.longitude());
    }

    private LocationDto toLocationDto(Location loc) {
        if (loc == null) {
            return null;
        }
        return new LocationDto(loc.getPlaceName(), loc.getLatitude(), loc.getLongitude());
    }

    private RideResponse toResponse(Ride ride) {
        return new RideResponse(
                ride.getId(),
                ride.getPassengerAccountId(),
                ride.getDriverAccountId(),
                toLocationDto(ride.getPickup()),
                toLocationDto(ride.getDestination()),
                ride.getServiceArea(),
                ride.getStatus(),
                ride.getEstimatedFare(),
                ride.getFinalFare(),
                ride.getCurrency(),
                ride.getPaymentStatus(),
                ride.getPaymentReceiptNumber(),
                ride.getCancellationReason(),
                ride.getRequestedAt(),
                ride.getAssignedAt(),
                ride.getAcceptedAt(),
                ride.getStartedAt(),
                ride.getCompletedAt(),
                ride.getCancelledAt(),
                ride.getUpdatedAt()
        );
    }
}
