package org.example.jubjubapi.ticket.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketNotAvailableException extends ServiceException {

    public TicketNotAvailableException() {
        super(HttpStatus.CONFLICT, "TICKET_NOT_AVAILABLE", "예약할 수 없는 티켓입니다.");
    }
}
