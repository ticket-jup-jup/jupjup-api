package org.example.jubjubapi.ticketserver.client.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketServerTicketDataNotFoundException extends ServiceException {
    public TicketServerTicketDataNotFoundException(String message) {
        super(HttpStatus.FORBIDDEN, "TICKET_DATA_NOT_FOUND", message);
    }
}