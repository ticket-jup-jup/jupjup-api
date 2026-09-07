package org.example.jubjubapi.ticket.webhook.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.ticket.detection.TicketDetectionSource;
import org.example.jubjubapi.ticket.detection.TicketStatusSyncService;
import org.example.jubjubapi.ticket.webhook.dto.TicketStatusWebhookRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/webhooks/tickets")
@RequiredArgsConstructor
public class TicketWebhookController {

    private final TicketStatusSyncService ticketStatusSyncService;

    @PostMapping
    public ResponseEntity<Void> receiveTicketStatus(
            @Valid @RequestBody TicketStatusWebhookRequest request
    ) {

        ticketStatusSyncService.syncStatus(
                request.getExternalTicketId(),
                request.getStatus(),
                TicketDetectionSource.WEBHOOK
        );

        return ResponseEntity.noContent().build();
    }
}