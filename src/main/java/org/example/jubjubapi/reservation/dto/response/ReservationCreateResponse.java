package org.example.jubjubapi.reservation.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationCreateResponse {

    private Long reservationId;
    private Long ticketId;
    private ReservationStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    @Builder
    private ReservationCreateResponse(Long reservationId, Long ticketId, ReservationStatus status, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.ticketId = ticketId;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static ReservationCreateResponse from(Reservation reservation) {
        return ReservationCreateResponse.builder()
                .reservationId(reservation.getId())
                .ticketId(reservation.getTicket().getId())
                .status(reservation.getStatus())
                .expiresAt(reservation.getExpiresAt())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
