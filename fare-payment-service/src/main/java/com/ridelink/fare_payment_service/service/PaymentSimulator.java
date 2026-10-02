package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.domain.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentSimulator {

    public PaymentStatus simulate(String method) {
        if (method == null) {
            return PaymentStatus.FAILED;
        }
        String normalized = method.trim().toUpperCase();
        if ("DECLINED".equals(normalized) || "FAIL".equals(normalized)) {
            return PaymentStatus.FAILED;
        }
        if ("CARD".equals(normalized) || "CASH".equals(normalized) || "WALLET".equals(normalized)) {
            return PaymentStatus.COMPLETED;
        }
        return PaymentStatus.FAILED;
    }
}
