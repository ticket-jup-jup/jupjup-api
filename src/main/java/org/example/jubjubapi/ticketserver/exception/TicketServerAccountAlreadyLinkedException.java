package org.example.jubjubapi.ticketserver.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketServerAccountAlreadyLinkedException extends ServiceException {

    public TicketServerAccountAlreadyLinkedException() {
        super(HttpStatus.CONFLICT, "TICKET_SERVER_ACCOUNT_ALREADY_LINKED",
                "이미 다른 사용자에게 연동된 티켓 서버 계정입니다.");
    }
}
