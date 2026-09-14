package org.example.jubjubapi.ticketserver.client.dto.response;

import lombok.Getter;
import org.example.jubjubapi.performance.dto.TicketServerPerformance;

import java.util.List;

@Getter
public class TicketServerPerformanceResponse {

    private final boolean success;
    private final List<TicketServerPerformance> data;

    public TicketServerPerformanceResponse(boolean success, List<TicketServerPerformance> data) {
        this.success = success;
        this.data = data;
    }
}