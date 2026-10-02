package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.FareRequest;
import com.ridelink.fare_payment_service.dto.FareResponse;
import com.ridelink.fare_payment_service.service.FarePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fares")
@Tag(name = "Fares")
@SecurityRequirement(name = "bearerAuth")
public class FareController {

    private final FarePaymentService farePaymentService;

    public FareController(FarePaymentService farePaymentService) {
        this.farePaymentService = farePaymentService;
    }

    @PostMapping("/estimate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN', 'SERVICE')")
    @Operation(summary = "Request fare estimate for pickup and destination")
    public FareResponse estimate(@Valid @RequestBody FareRequest request) {
        return farePaymentService.estimateFare(request);
    }

    @PostMapping("/calculate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN', 'SERVICE')")
    @Operation(summary = "Calculate final fare for a completed ride")
    public FareResponse calculate(@Valid @RequestBody FareRequest request) {
        return farePaymentService.calculateFinalFare(request);
    }

    @GetMapping("/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN', 'SERVICE')")
    @Operation(summary = "Get latest fare quote by ride id")
    public FareResponse getByRideId(@PathVariable String rideId) {
        return farePaymentService.getLatestQuote(rideId);
    }
}
