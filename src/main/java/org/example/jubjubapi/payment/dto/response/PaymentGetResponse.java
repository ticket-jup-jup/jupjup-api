package org.example.jubjubapi.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.jubjubapi.payment.entity.Payment;
import org.example.jubjubapi.payment.entity.PaymentMethod;
import org.example.jubjubapi.payment.entity.PaymentStatus;
import org.example.jubjubapi.ticket.dto.TicketInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PaymentGetResponse {

    private final Long id;
    private final BigDecimal amount;
    private final PaymentMethod paymentMethod;
    private final PaymentStatus status;
    private final LocalDateTime paidAt;
    private final Long reservationId;
    private final TicketInfo ticket;

    @Builder
    private PaymentGetResponse(Long id, BigDecimal amount, PaymentMethod paymentMethod, PaymentStatus status, LocalDateTime paidAt, Long reservationId, TicketInfo ticket) {
        this.id = id;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paidAt = paidAt;
        this.reservationId = reservationId;
        this.ticket = ticket;
    }

    public static PaymentGetResponse from(Payment payment) {
        return PaymentGetResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .reservationId(payment.getReservation().getId())
                .ticket(TicketInfo.from(payment.getReservation().getTicket()))
                .build();
    }
}
