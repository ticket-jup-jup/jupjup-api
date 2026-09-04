package org.example.jubjubapi.ticket.client.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketServerRequestRejectedException extends ServiceException {
    public TicketServerRequestRejectedException() {
        super(HttpStatus.CONFLICT, "TICKET_SERVER_REQUEST_REJECTED", "티켓 서버에서 요청을 처리할 수 없습니다");
    }
}
