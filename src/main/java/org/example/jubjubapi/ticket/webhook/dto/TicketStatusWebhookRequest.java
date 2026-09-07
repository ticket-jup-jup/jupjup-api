package org.example.jubjubapi.ticket.webhook.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.ticket.entity.TicketStatus;

@Getter
@NoArgsConstructor
public class TicketStatusWebhookRequest {

    @NotNull
    private Long externalTicketId;

    @NotNull
    private Long performanceId;

    @NotNull
    private TicketStatus status;
}