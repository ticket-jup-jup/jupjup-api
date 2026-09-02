package org.example.jubjubapi.ticket.dto;


import lombok.Getter;
import org.example.jubjubapi.ticket.entity.TicketWatch;
import org.example.jubjubapi.ticket.entity.TicketWatchStatus;

import java.time.LocalDateTime;

@Getter
public class TicketWatchResponse {
    private final Long id;
    private final Long ticketId;
    private final TicketWatchStatus status;
    private final TicketResponse ticket;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TicketWatchResponse(Long id, Long ticketId, TicketWatchStatus status,
                               TicketResponse ticket, LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.status = status;
        this.ticket = ticket;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TicketWatchResponse from(TicketWatch watch) {
        return new TicketWatchResponse(watch.getId(), watch.getTicket().getId(),
                watch.getStatus(), TicketResponse.from(watch.getTicket()),
                watch.getCreatedAt(), watch.getUpdatedAt());
    }
}