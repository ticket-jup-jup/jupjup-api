package org.example.jubjubapi.reservation.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.entity.ReservationStatus;
import org.example.jubjubapi.ticket.dto.TicketInfo;

import java.time.LocalDateTime;

@Getter
public class ReservationGetResponse {

    private final Long id;
    private final ReservationStatus status;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;
    private final TicketInfo ticket;

    @Builder
    private ReservationGetResponse(Long id, ReservationStatus status, LocalDateTime expiresAt, LocalDateTime createdAt, TicketInfo ticket) {
        this.id = id;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.ticket = ticket;
    }

    public static ReservationGetResponse from(Reservation reservation) {
        return ReservationGetResponse.builder()
                .id(reservation.getId())
                .status(reservation.getStatus())
                .expiresAt(reservation.getExpiresAt())
                .createdAt(reservation.getCreatedAt())
                .ticket(TicketInfo.from(reservation.getTicket()))
                .build();
    }
}
