package org.example.jubjubapi.reservation.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.entity.ReservationStatus;
import org.example.jubjubapi.ticket.entity.Ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ReservationCancelResponse {

    private final Long id;
    private final ReservationStatus status;
    private final TicketInfo ticket;

    @Builder
    private ReservationCancelResponse(Long id, ReservationStatus status, TicketInfo ticket) {
        this.id = id;
        this.status = status;
        this.ticket = ticket;
    }

    public static ReservationCancelResponse from(Reservation reservation) {
        return ReservationCancelResponse.builder()
                .id(reservation.getId())
                .status(reservation.getStatus())
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
