package org.example.jubjubapi.reservation.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.entity.ReservationStatus;
import org.example.jubjubapi.ticket.dto.TicketInfo;

@Getter
public class ReservationCancelResponse {

    private final Long id;
    private final ReservationStatus status;

    @Builder
    private ReservationCancelResponse(Long id, ReservationStatus status) {
        this.id = id;
        this.status = status;
    }

    public static ReservationCancelResponse from(Reservation reservation) {
        return ReservationCancelResponse.builder()
                .id(reservation.getId())
                .status(reservation.getStatus())
                .build();
    }
}
