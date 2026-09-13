package org.example.jubjubapi.ticketserver.client.dto.response;

import lombok.Getter;
import org.example.jubjubapi.seat.dto.TicketServerSeat;

import java.util.List;

@Getter
public class TicketServerSeatResponse {

    private final boolean success;
    private final List<TicketServerSeat> data;

    public TicketServerSeatResponse(boolean success, List<TicketServerSeat> data) {
        this.success = success;
        this.data = data;
    }
}