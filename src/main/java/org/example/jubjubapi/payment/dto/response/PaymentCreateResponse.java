package org.example.jubjubapi.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.jubjubapi.payment.entity.Payment;
import org.example.jubjubapi.payment.entity.PaymentMethod;
import org.example.jubjubapi.payment.entity.PaymentStatus;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.performance.entity.Performance;

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
    private final TicketInfo ticket;

    @Builder
    private PaymentCreateResponse(Long id, Long reservationId, PaymentStatus status, BigDecimal amount, PaymentMethod paymentMethod, LocalDateTime paidAt, TicketInfo ticket) {
        this.id = id;
        this.reservationId = reservationId;
        this.status = status;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
        this.ticket = ticket;
    }

    public static PaymentCreateResponse from(Payment payment, Reservation reservation) {
        return PaymentCreateResponse.builder()
                .id(payment.getId())
                .reservationId(reservation.getId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .ticket(TicketInfo.from(reservation.getTicket()))
                .build();
    }

    @Getter
    @Builder
    public static class TicketInfo {

        private final Long id;
        private final Long performanceId;
        private final Long programId;
        private final LocalDateTime startAt;
        private final String venue;

        public static TicketInfo from(Ticket ticket) {
            Performance performance = ticket.getPerformance();

            return TicketInfo.builder()
                    .id(ticket.getId())
                    .performanceId(performance.getId())
                    .programId(performance.getProgramId())
                    .startAt(performance.getStartAt())
                    .venue(performance.getVenue())
                    .build();
        }
    }
}
