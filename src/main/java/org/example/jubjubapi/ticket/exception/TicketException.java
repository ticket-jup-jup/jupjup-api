package org.example.jubjubapi.ticket.exception;

import org.example.jubjubapi.global.exception.ServiceException;

public class TicketException extends ServiceException {
    public TicketException(TicketErrorCode error) {
        super(error.getStatus(),error.getCode(), error.getMessage());
    }


}
