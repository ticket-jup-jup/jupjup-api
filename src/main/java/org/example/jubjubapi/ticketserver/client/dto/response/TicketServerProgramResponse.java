package org.example.jubjubapi.ticketserver.client.dto.response;

import lombok.Getter;
import org.example.jubjubapi.program.dto.TicketServerProgram;

import java.util.List;

@Getter
public class TicketServerProgramResponse {

    private final boolean success;
    private final List<TicketServerProgram> data;

    public TicketServerProgramResponse(boolean success, List<TicketServerProgram> data) {
        this.success = success;
        this.data = data;
    }
}