package org.example.jubjubapi.ticket.client.dto.request;

import lombok.Getter;

@Getter
public class TicketServerReservationRequest {

    private final Long userId;
    private final Long ticketId;

    public TicketServerReservationRequest(Long userId, Long ticketId) {
        this.userId = userId;
        this.ticketId = ticketId;
    }
}
