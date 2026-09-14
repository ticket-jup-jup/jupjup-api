package org.example.jubjubapi.ticketserver.client.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketServerSeatDataNotFoundException extends ServiceException {
    public TicketServerSeatDataNotFoundException(String message) {
        super(HttpStatus.FORBIDDEN, "SEAT_DATA_NOT_FOUND", message);
    }
}