package org.example.jubjubapi.ticket.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketNotFoundException extends ServiceException {

    public TicketNotFoundException() {
        super(HttpStatus.NOT_FOUND, "TICKET_NOT_FOUND", "티켓을 찾을 수 없습니다.");
    }
}
