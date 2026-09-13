package org.example.jubjubapi.ticket.dto;

import lombok.Getter;
import org.example.jubjubapi.ticket.entity.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class TicketServerTicket {

    private final Long id;
    private final Long performanceId;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final String venue;
    private final Long seatId;
    private final BigDecimal price;
    private final TicketStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TicketServerTicket(Long id, Long performanceId, LocalDateTime startAt, LocalDateTime endAt, String venue, Long seatId, BigDecimal price, TicketStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.performanceId = performanceId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.venue = venue;
        this.seatId = seatId;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}