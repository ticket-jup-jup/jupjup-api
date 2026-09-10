package org.example.jubjubapi.ticket.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.jubjubapi.ticket.entity.Ticket;

import java.math.BigDecimal;

@Getter
public class TicketInfo {

    private final Long ticketId;
    private final Long performanceId;
    private final BigDecimal price;

    @Builder
    private TicketInfo(Long ticketId, Long performanceId, BigDecimal price) {
        this.ticketId = ticketId;
        this.performanceId = performanceId;
        this.price = price;
    }

    public static TicketInfo from(Ticket ticket) {
        return TicketInfo.builder()
                .ticketId(ticket.getId())
                .performanceId(ticket.getPerformanceId())
                .price(ticket.getPrice())
                .build();
    }
}
