package org.example.jubjubapi.ticket.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class InvalidTicketPriceException extends ServiceException {

    public InvalidTicketPriceException(){
        super(
                HttpStatus.BAD_REQUEST,
                "INVALID_TICKET_PRICE",
                "티켓 가격은 필수이며 0 이상이어야 합니다."
        );
    }
}
