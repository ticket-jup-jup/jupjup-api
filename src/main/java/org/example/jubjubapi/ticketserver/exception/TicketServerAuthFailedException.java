package org.example.jubjubapi.ticketserver.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketServerAuthFailedException extends ServiceException {

    public TicketServerAuthFailedException() {
        super(HttpStatus.BAD_REQUEST, "TICKET_SERVER_AUTH_FAILED",
                "티켓 서버 이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}

