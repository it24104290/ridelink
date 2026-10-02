package com.ridelink.fare_payment_service.repository;

import com.ridelink.fare_payment_service.domain.PaymentRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRecordRepository extends MongoRepository<PaymentRecord, String> {

    Optional<PaymentRecord> findFirstByRideIdOrderByCreatedAtDesc(String rideId);

    Optional<PaymentRecord> findByReceiptNumber(String receiptNumber);

    List<PaymentRecord> findByPassengerAccountId(String passengerAccountId);
}
