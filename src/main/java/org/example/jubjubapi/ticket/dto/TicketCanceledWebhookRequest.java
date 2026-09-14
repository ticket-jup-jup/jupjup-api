package org.example.jubjubapi.ticket.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class TicketCanceledWebhookRequest {
    private final Long ticketId;
    private final Long performanceId;
    private final Long seatId;
    private final BigDecimal price;
    private final LocalDateTime canceledAt;

    public TicketCanceledWebhookRequest(Long ticketId, Long performanceId, Long seatId, BigDecimal price, LocalDateTime canceledAt) {
        this.ticketId = ticketId;
        this.performanceId = performanceId;
        this.seatId = seatId;
        this.price = price;
        this.canceledAt = canceledAt;
    }
}