package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.domain.FareQuote;
import com.ridelink.fare_payment_service.domain.GeoPoint;
import com.ridelink.fare_payment_service.domain.PaymentRecord;
import com.ridelink.fare_payment_service.domain.PaymentStatus;
import com.ridelink.fare_payment_service.dto.FareRequest;
import com.ridelink.fare_payment_service.dto.FareResponse;
import com.ridelink.fare_payment_service.dto.LocationDto;
import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.dto.PaymentResponse;
import com.ridelink.fare_payment_service.dto.ReceiptResponse;
import com.ridelink.fare_payment_service.exception.ApiException;
import com.ridelink.fare_payment_service.repository.FareQuoteRepository;
import com.ridelink.fare_payment_service.repository.PaymentRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class FarePaymentService {

    private final FareCalculator fareCalculator;
    private final PaymentSimulator paymentSimulator;
    private final FareQuoteRepository fareQuoteRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final String defaultCurrency;

    public FarePaymentService(
            FareCalculator fareCalculator,
            PaymentSimulator paymentSimulator,
            FareQuoteRepository fareQuoteRepository,
            PaymentRecordRepository paymentRecordRepository,
            @Value("${ridelink.fare.currency:LKR}") String defaultCurrency) {
        this.fareCalculator = fareCalculator;
        this.paymentSimulator = paymentSimulator;
        this.fareQuoteRepository = fareQuoteRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.defaultCurrency = defaultCurrency;
    }

    public FareResponse estimateFare(FareRequest request) {
        return createQuote(request, false);
    }

    public FareResponse calculateFinalFare(FareRequest request) {
        return createQuote(request, true);
    }

    public FareResponse getLatestQuote(String rideId) {
        FareQuote quote = fareQuoteRepository.findFirstByRideIdOrderByCreatedAtDesc(rideId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Fare quote not found for ride " + rideId));
        return toFareResponse(quote);
    }

    public PaymentResponse recordPayment(String passengerAccountId, PaymentRequest request) {
        PaymentStatus status = paymentSimulator.simulate(request.method());
        String receiptNumber = status == PaymentStatus.COMPLETED
                ? "RCP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                : null;

        PaymentRecord record = new PaymentRecord();
        record.setRideId(request.rideId());
        record.setPassengerAccountId(passengerAccountId);
        record.setAmount(request.amount());
        record.setCurrency(request.currency() != null && !request.currency().isBlank() ? request.currency() : defaultCurrency);
        record.setMethod(request.method().trim().toUpperCase());
        record.setStatus(status);
        record.setReceiptNumber(receiptNumber);
        record.setCreatedAt(Instant.now());

        PaymentRecord saved = paymentRecordRepository.save(record);
        return toPaymentResponse(saved);
    }

    public PaymentResponse getPaymentById(String paymentId) {
        PaymentRecord record = paymentRecordRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment record not found"));
        return toPaymentResponse(record);
    }

    public PaymentResponse getPaymentByRideId(String rideId) {
        PaymentRecord record = paymentRecordRepository.findFirstByRideIdOrderByCreatedAtDesc(rideId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment record not found for ride " + rideId));
        return toPaymentResponse(record);
    }

    public ReceiptResponse getReceipt(String receiptNumber) {
        PaymentRecord record = paymentRecordRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Receipt not found: " + receiptNumber));
        return toReceiptResponse(record);
    }

    public ReceiptResponse getReceiptByRideId(String rideId) {
        PaymentRecord record = paymentRecordRepository.findFirstByRideIdOrderByCreatedAtDesc(rideId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No payment found for ride " + rideId));
        if (record.getStatus() != PaymentStatus.COMPLETED || record.getReceiptNumber() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No valid receipt available for ride " + rideId);
        }
        return toReceiptResponse(record);
    }

    private FareResponse createQuote(FareRequest request, boolean isFinal) {
        double distance = fareCalculator.distanceKm(
                request.pickup().latitude(), request.pickup().longitude(),
                request.destination().latitude(), request.destination().longitude());
        double duration = fareCalculator.durationMin(distance);
        double amount = fareCalculator.calculate(distance, duration);

        FareQuote quote = new FareQuote();
        quote.setRideId(request.rideId());
        quote.setPickup(toGeoPoint(request.pickup()));
        quote.setDestination(toGeoPoint(request.destination()));
        quote.setDistanceKm(distance);
        quote.setDurationMin(duration);
        quote.setAmount(amount);
        quote.setCurrency(defaultCurrency);
        quote.setRuleVersion(FareCalculator.RULE_VERSION);
        quote.setFinalFare(isFinal);
        quote.setCreatedAt(Instant.now());

        FareQuote saved = fareQuoteRepository.save(quote);
        return toFareResponse(saved);
    }

    private GeoPoint toGeoPoint(LocationDto dto) {
        GeoPoint point = new GeoPoint();
        point.setPlaceName(dto.placeName());
        point.setLatitude(dto.latitude());
        point.setLongitude(dto.longitude());
        return point;
    }

    private FareResponse toFareResponse(FareQuote quote) {
        return new FareResponse(
                quote.getId(),
                quote.getRideId(),
                quote.getDistanceKm(),
                quote.getDurationMin(),
                quote.getAmount(),
                quote.getCurrency(),
                quote.getRuleVersion(),
                quote.isFinalFare()
        );
    }

    private PaymentResponse toPaymentResponse(PaymentRecord record) {
        return new PaymentResponse(
                record.getId(),
                record.getRideId(),
                record.getPassengerAccountId(),
                record.getAmount(),
                record.getCurrency(),
                record.getMethod(),
                record.getStatus(),
                record.getReceiptNumber(),
                record.getCreatedAt()
        );
    }

    private ReceiptResponse toReceiptResponse(PaymentRecord record) {
        return new ReceiptResponse(
                record.getReceiptNumber(),
                record.getId(),
                record.getRideId(),
                record.getPassengerAccountId(),
                record.getAmount(),
                record.getCurrency(),
                record.getMethod(),
                record.getStatus(),
                record.getCreatedAt()
        );
    }
}
