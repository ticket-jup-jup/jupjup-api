package org.example.jubjubapi.ticketserver.client.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketServerProgramDataNotFoundException extends ServiceException {
    public TicketServerProgramDataNotFoundException(String message) {
        super(HttpStatus.FORBIDDEN, "PROGRAM_DATA_NOT_FOUND", message);
    }
}