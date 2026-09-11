package org.example.jubjubapi.ticketserver.client.dto.request;

import lombok.Getter;

@Getter
public class TicketServerConfirmRequest {

    private final String paymentMethod;

    public TicketServerConfirmRequest(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
