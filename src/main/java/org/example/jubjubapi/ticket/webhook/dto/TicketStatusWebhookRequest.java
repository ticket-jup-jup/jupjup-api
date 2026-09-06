package org.example.jubjubapi.ticket.webhook.dto;

import jakarta.validation.constraints.NotNull;
import org.example.jubjubapi.ticket.entity.TicketStatus;

public record TicketStatusWebhookRequest (
    @NotNull
    Long externalTicketId,

    @NotNull
    Long performanceId,

    @NotNull
    TicketStatus status
){}
