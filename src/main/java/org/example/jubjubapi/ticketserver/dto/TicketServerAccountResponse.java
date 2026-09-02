package org.example.jubjubapi.ticketserver.dto;

import org.example.jubjubapi.ticketserver.entity.TicketServerAccount;

import java.time.LocalDateTime;

public record TicketServerAccountResponse(
        Long externalUserId,
        LocalDateTime linkedAt
) {
    public static TicketServerAccountResponse from(TicketServerAccount account) {
        return new TicketServerAccountResponse(account.getExternalUserId(), account.getCreatedAt());
    }
}
