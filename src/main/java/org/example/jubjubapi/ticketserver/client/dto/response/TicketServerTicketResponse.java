package org.example.jubjubapi.ticketserver.client.dto.response;

import lombok.Getter;
import org.example.jubjubapi.ticket.dto.TicketServerTicket;

import java.util.List;

@Getter
public class TicketServerTicketResponse {

    private final boolean success;
    private final List<TicketServerTicket> data;

    public TicketServerTicketResponse(boolean success, List<TicketServerTicket> data) {
        this.success = success;
        this.data = data;
    }
}