package org.example.jubjubapi.ticket.dto;

import lombok.Getter;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class TicketResponse {
    private final Long id;
    private final Long externalTicketId;
    private final BigDecimal price;
    private final TicketStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TicketResponse(Long id, Long externalTicketId, BigDecimal price, TicketStatus status,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.externalTicketId = externalTicketId;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getExternalTicketId(), ticket.getPrice(),
                ticket.getStatus(), ticket.getCreatedAt(), ticket.getUpdatedAt());
    }
}
