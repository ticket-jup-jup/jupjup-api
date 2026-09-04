package org.example.jubjubapi.ticket.client.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketServerApiException extends ServiceException {

    public TicketServerApiException() {
        super(HttpStatus.BAD_GATEWAY, "TICKET_SERVER_API_ERROR", "티켓 서버 요청에 실패했습니다");
    }
}
