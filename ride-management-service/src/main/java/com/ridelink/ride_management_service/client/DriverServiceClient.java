package com.ridelink.ride_management_service.client;

import com.ridelink.ride_management_service.dto.ExternalDriverDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class DriverServiceClient implements DriverClient {

    private static final Logger log = LoggerFactory.getLogger(DriverServiceClient.class);

    private final RestTemplate restTemplate;
    private final String driverServiceUrl;
    private final String internalApiKey;

    public DriverServiceClient(
            RestTemplate restTemplate,
            @Value("${ridelink.services.driver-url:http://localhost:8082}") String driverServiceUrl,
            @Value("${ridelink.security.internal-api-key}") String internalApiKey) {
        this.restTemplate = restTemplate;
        this.driverServiceUrl = driverServiceUrl;
        this.internalApiKey = internalApiKey;
    }

    public List<ExternalDriverDto> findEligibleDrivers(String serviceArea, double latitude, double longitude) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(driverServiceUrl + "/api/drivers/available")
                    .queryParam("serviceArea", serviceArea)
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Api-Key", internalApiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<ExternalDriverDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {});

            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Failed to query eligible drivers from Driver Service: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean updateDriverAvailability(String driverAccountId, boolean available) {
        try {
            String url = driverServiceUrl + "/api/drivers/" + driverAccountId + "/availability";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Api-Key", internalApiKey);

            Map<String, Boolean> body = Map.of("available", available);
            HttpEntity<Map<String, Boolean>> entity = new HttpEntity<>(body, headers);

            restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
            return true;
        } catch (Exception ex) {
            log.warn("Failed to update availability for driver {} to {}: {}", driverAccountId, available, ex.getMessage());
            return false;
        }
    }
}
