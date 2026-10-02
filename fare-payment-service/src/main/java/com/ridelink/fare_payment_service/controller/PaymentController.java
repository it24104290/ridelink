package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.dto.PaymentResponse;
import com.ridelink.fare_payment_service.dto.ReceiptResponse;
import com.ridelink.fare_payment_service.service.FarePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final FarePaymentService farePaymentService;

    public PaymentController(FarePaymentService farePaymentService) {
        this.farePaymentService = farePaymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN', 'SERVICE')")
    @Operation(summary = "Record simulated payment")
    public PaymentResponse pay(
            Authentication authentication,
            @RequestHeader(value = "X-Passenger-Id", required = false) String passengerHeader,
            @Valid @RequestBody PaymentRequest request) {
        String passengerId = (passengerHeader != null && !passengerHeader.isBlank())
                ? passengerHeader
                : authentication.getName();
        return farePaymentService.recordPayment(passengerId, request);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN', 'SERVICE')")
    @Operation(summary = "Get payment details by payment id")
    public PaymentResponse getById(@PathVariable String paymentId) {
        return farePaymentService.getPaymentById(paymentId);
    }

    @GetMapping("/ride/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN', 'SERVICE')")
    @Operation(summary = "Get payment details by ride id")
    public PaymentResponse getByRideId(@PathVariable String rideId) {
        return farePaymentService.getPaymentByRideId(rideId);
    }
}
