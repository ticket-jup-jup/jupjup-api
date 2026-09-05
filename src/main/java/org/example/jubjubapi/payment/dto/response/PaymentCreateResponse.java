package org.example.jubjubapi.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.jubjubapi.payment.entity.Payment;
import org.example.jubjubapi.payment.entity.PaymentMethod;
import org.example.jubjubapi.payment.entity.PaymentStatus;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.ticket.entity.Ticket;

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
    public static class TicketInfo {
        private final Long id;
        private final String programName;
        private final LocalDateTime startAt;
        private final String venue;
        private final String seatGrade;
        private final String section;
        private final String rowNumber;
        private final String seatNumber;
        private final BigDecimal price;

        @Builder
        private TicketInfo(Long id, String programName, LocalDateTime startAt, String venue, String seatGrade, String section, String rowNumber, String seatNumber, BigDecimal price) {
            this.id = id;
            this.programName = programName;
            this.startAt = startAt;
            this.venue = venue;
            this.seatGrade = seatGrade;
            this.section = section;
            this.rowNumber = rowNumber;
            this.seatNumber = seatNumber;
            this.price = price;
        }

        public static TicketInfo from(Ticket ticket) {
            return TicketInfo.builder()
                    .id(ticket.getId())
                    .programName(ticket.getProgramName())
                    .startAt(ticket.getStartAt())
                    .venue(ticket.getVenue())
                    .seatGrade(ticket.getSeatGrade())
                    .section(ticket.getSection())
                    .rowNumber(ticket.getRowNumber())
                    .seatNumber(ticket.getSeatNumber())
                    .price(ticket.getPrice())
                    .build();
        }
    }
}
