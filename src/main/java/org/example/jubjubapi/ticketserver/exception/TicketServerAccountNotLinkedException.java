package org.example.jubjubapi.ticketserver.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketServerAccountNotLinkedException extends ServiceException {

    public TicketServerAccountNotLinkedException() {
        super(HttpStatus.CONFLICT, "TICKET_SERVER_ACCOUNT_NOT_LINKED", "티켓 서버 계정 연동이 필요합니다.");
    }
}
