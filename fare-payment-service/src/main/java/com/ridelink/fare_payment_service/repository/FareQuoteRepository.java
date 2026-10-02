package com.ridelink.fare_payment_service.repository;

import com.ridelink.fare_payment_service.domain.FareQuote;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FareQuoteRepository extends MongoRepository<FareQuote, String> {

    Optional<FareQuote> findFirstByRideIdAndFinalFareTrueOrderByCreatedAtDesc(String rideId);

    Optional<FareQuote> findFirstByRideIdOrderByCreatedAtDesc(String rideId);
}
