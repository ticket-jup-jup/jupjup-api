package org.example.jubjubapi.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.jubjubapi.payment.entity.Payment;
import org.example.jubjubapi.payment.entity.PaymentStatus;

import java.math.BigDecimal;

@Getter
public class PaymentCancelResponse {

    private final Long id;
    private final PaymentStatus status;
    private final BigDecimal amount;

    @Builder
    private PaymentCancelResponse(Long id, PaymentStatus status, BigDecimal amount) {
        this.id = id;
        this.status = status;
        this.amount = amount;
    }

    public static PaymentCancelResponse from(Payment payment) {
        return PaymentCancelResponse.builder()
                .id(payment.getId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .build();
    }
}
