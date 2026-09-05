package org.example.jubjubapi.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.jubjubapi.payment.entity.Payment;
import org.example.jubjubapi.payment.entity.PaymentMethod;
import org.example.jubjubapi.payment.entity.PaymentStatus;
import org.example.jubjubapi.ticket.entity.Ticket;

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
