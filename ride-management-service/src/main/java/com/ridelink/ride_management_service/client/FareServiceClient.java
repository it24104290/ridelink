package com.ridelink.ride_management_service.client;

import com.ridelink.ride_management_service.dto.ExternalFareDto;
import com.ridelink.ride_management_service.dto.ExternalPaymentDto;
import com.ridelink.ride_management_service.dto.LocationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Component
public class FareServiceClient implements FareClient {

    private static final Logger log = LoggerFactory.getLogger(FareServiceClient.class);

    private final RestTemplate restTemplate;
    private final String fareServiceUrl;
    private final String internalApiKey;

    public FareServiceClient(
            RestTemplate restTemplate,
            @Value("${ridelink.services.fare-url:http://localhost:8084}") String fareServiceUrl,
            @Value("${ridelink.security.internal-api-key}") String internalApiKey) {
        this.restTemplate = restTemplate;
        this.fareServiceUrl = fareServiceUrl;
        this.internalApiKey = internalApiKey;
    }

    public Optional<ExternalFareDto> estimateFare(String rideId, LocationDto pickup, LocationDto destination) {
        try {
            String url = fareServiceUrl + "/api/fares/estimate";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Api-Key", internalApiKey);

            Map<String, Object> body = Map.of(
                    "rideId", rideId != null ? rideId : "",
                    "pickup", pickup,
                    "destination", destination
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<ExternalFareDto> response = restTemplate.postForEntity(url, entity, ExternalFareDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (Exception ex) {
            log.warn("Failed to obtain fare estimate from Fare Service: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ExternalFareDto> calculateFinalFare(String rideId, LocationDto pickup, LocationDto destination) {
        try {
            String url = fareServiceUrl + "/api/fares/calculate";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Api-Key", internalApiKey);

            Map<String, Object> body = Map.of(
                    "rideId", rideId != null ? rideId : "",
                    "pickup", pickup,
                    "destination", destination
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<ExternalFareDto> response = restTemplate.postForEntity(url, entity, ExternalFareDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (Exception ex) {
            log.warn("Failed to calculate final fare from Fare Service: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ExternalPaymentDto> recordPayment(String passengerAccountId, String rideId, double amount, String method, String currency) {
        try {
            String url = fareServiceUrl + "/api/payments";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Api-Key", internalApiKey);
            headers.set("X-Passenger-Id", passengerAccountId);

            Map<String, Object> body = Map.of(
                    "rideId", rideId,
                    "amount", amount,
                    "method", method,
                    "currency", currency != null ? currency : "LKR"
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<ExternalPaymentDto> response = restTemplate.postForEntity(url, entity, ExternalPaymentDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (Exception ex) {
            log.warn("Failed to record payment in Fare Service: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
