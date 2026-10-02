package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.ReceiptResponse;
import com.ridelink.fare_payment_service.service.FarePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipts")
@Tag(name = "Receipts")
@SecurityRequirement(name = "bearerAuth")
public class ReceiptController {

    private final FarePaymentService farePaymentService;

    public ReceiptController(FarePaymentService farePaymentService) {
        this.farePaymentService = farePaymentService;
    }

    @GetMapping("/{receiptNumber}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN', 'SERVICE')")
    @Operation(summary = "Retrieve payment receipt by receipt number")
    public ReceiptResponse getReceipt(@PathVariable String receiptNumber) {
        return farePaymentService.getReceipt(receiptNumber);
    }

    @GetMapping("/ride/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN', 'SERVICE')")
    @Operation(summary = "Retrieve payment receipt by ride id")
    public ReceiptResponse getReceiptByRideId(@PathVariable String rideId) {
        return farePaymentService.getReceiptByRideId(rideId);
    }
}
