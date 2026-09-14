package org.example.jubjubapi.notification.event;

import java.time.LocalDateTime;

public record TicketCanceledEvent(
        String eventId,
        Long ticketId,
        Long performanceId,
        LocalDateTime canceledAt
) {
}