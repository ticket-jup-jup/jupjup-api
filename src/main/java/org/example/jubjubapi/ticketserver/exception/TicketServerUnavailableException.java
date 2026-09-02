package org.example.jubjubapi.ticketserver.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketServerUnavailableException extends ServiceException {

    public TicketServerUnavailableException() {
        super(HttpStatus.BAD_GATEWAY, "TICKET_SERVER_UNAVAILABLE",
                "티켓 서버와 통신할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }
}