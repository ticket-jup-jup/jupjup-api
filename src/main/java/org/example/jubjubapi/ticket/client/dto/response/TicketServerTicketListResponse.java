package org.example.jubjubapi.ticket.client.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TicketServerTicketListResponse {

    private boolean success;
    private List<TicketServerTicketResponse> data;
    private Object error;
}
//polling list response