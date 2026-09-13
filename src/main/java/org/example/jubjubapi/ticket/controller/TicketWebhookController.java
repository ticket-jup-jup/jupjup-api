package org.example.jubjubapi.ticket.controller;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.global.dto.ApiResponse;
import org.example.jubjubapi.ticket.dto.TicketCanceledWebhookRequest;
import org.example.jubjubapi.ticket.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/webhooks")
public class TicketWebhookController {
    private final TicketService ticketService;

    @PostMapping("/tickets/canceled")
    public ResponseEntity<ApiResponse<Void>> ticketCanceled(
            @RequestBody TicketCanceledWebhookRequest request
    ) {
        ticketService.handleTicketCanceled(request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}