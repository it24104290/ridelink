package com.ridelink.ride_management_service.client;

import com.ridelink.ride_management_service.dto.ExternalFareDto;
import com.ridelink.ride_management_service.dto.ExternalPaymentDto;
import com.ridelink.ride_management_service.dto.LocationDto;

import java.util.Optional;

public interface FareClient {

    Optional<ExternalFareDto> estimateFare(String rideId, LocationDto pickup, LocationDto destination);

    Optional<ExternalFareDto> calculateFinalFare(String rideId, LocationDto pickup, LocationDto destination);

    Optional<ExternalPaymentDto> recordPayment(String passengerAccountId, String rideId, double amount, String method, String currency);
}
