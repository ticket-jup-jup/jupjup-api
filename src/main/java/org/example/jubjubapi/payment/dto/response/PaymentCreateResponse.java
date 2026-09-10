package org.example.jubjubapi.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.jubjubapi.payment.entity.Payment;
import org.example.jubjubapi.payment.entity.PaymentMethod;
import org.example.jubjubapi.payment.entity.PaymentStatus;
import org.example.jubjubapi.reservation.entity.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PaymentCreateResponse {

    private final Long id;
    private final Long reservationId;
    private final PaymentStatus status;
    private final BigDecimal amount;
    private final PaymentMethod paymentMethod;
    private final LocalDateTime paidAt;

    @Builder
    private PaymentCreateResponse(Long id, Long reservationId, PaymentStatus status, BigDecimal amount, PaymentMethod paymentMethod, LocalDateTime paidAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.status = status;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
    }

    public static PaymentCreateResponse from(Payment payment, Reservation reservation) {
        return PaymentCreateResponse.builder()
                .id(payment.getId())
                .reservationId(reservation.getId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
