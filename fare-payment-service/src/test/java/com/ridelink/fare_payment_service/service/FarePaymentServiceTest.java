package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.domain.PaymentRecord;
import com.ridelink.fare_payment_service.domain.PaymentStatus;
import com.ridelink.fare_payment_service.dto.FareRequest;
import com.ridelink.fare_payment_service.dto.LocationDto;
import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.exception.ApiException;
import com.ridelink.fare_payment_service.repository.FareQuoteRepository;
import com.ridelink.fare_payment_service.repository.PaymentRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarePaymentServiceTest {

    @Mock
    private FareQuoteRepository fareQuoteRepository;

    @Mock
    private PaymentRecordRepository paymentRecordRepository;

    private FareCalculator fareCalculator;
    private PaymentSimulator paymentSimulator;
    private FarePaymentService service;

    @BeforeEach
    void setUp() {
        fareCalculator = new FareCalculator(150.0, 80.0, 5.0, 30.0);
        paymentSimulator = new PaymentSimulator();
        service = new FarePaymentService(fareCalculator, paymentSimulator, fareQuoteRepository, paymentRecordRepository, "LKR");
    }

    @Test
    void estimateFareCalculatesCorrectAmount() {
        LocationDto pickup = new LocationDto("Colombo Fort", 6.9344, 79.8428);
        LocationDto dest = new LocationDto("Galle Face", 6.9271, 79.8440);
        FareRequest req = new FareRequest("ride-123", pickup, dest);

        when(fareQuoteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.estimateFare(req);

        assertNotNull(response);
        assertTrue(response.distanceKm() > 0);
        assertTrue(response.amount() >= 150.0);
        assertEquals("FARE-RULE-V1", response.ruleVersion());
    }

    @Test
    void paymentSuccessfulGeneratesReceipt() {
        PaymentRequest req = new PaymentRequest("ride-100", 500.0, "CARD", "LKR");

        when(paymentRecordRepository.save(any(PaymentRecord.class))).thenAnswer(inv -> {
            PaymentRecord r = inv.getArgument(0);
            r.setId("pay-001");
            return r;
        });

        var res = service.recordPayment("acc-passenger-001", req);

        assertEquals(PaymentStatus.COMPLETED, res.status());
        assertNotNull(res.receiptNumber());
        assertTrue(res.receiptNumber().startsWith("RCP-"));
    }

    @Test
    void failedPaymentHasNoReceipt() {
        PaymentRequest req = new PaymentRequest("ride-101", 500.0, "DECLINED", "LKR");

        when(paymentRecordRepository.save(any(PaymentRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = service.recordPayment("acc-passenger-001", req);

        assertEquals(PaymentStatus.FAILED, res.status());
        org.junit.jupiter.api.Assertions.assertNull(res.receiptNumber());
    }

    @Test
    void getReceiptThrowsNotFoundWhenMissing() {
        when(paymentRecordRepository.findByReceiptNumber("RCP-NONEXISTENT")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.getReceipt("RCP-NONEXISTENT"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
