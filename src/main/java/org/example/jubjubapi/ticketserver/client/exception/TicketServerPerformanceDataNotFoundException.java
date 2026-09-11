package org.example.jubjubapi.ticketserver.client.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class TicketServerPerformanceDataNotFoundException extends ServiceException {
    public TicketServerPerformanceDataNotFoundException(String message) {
        super(HttpStatus.FORBIDDEN, "PERFORMANCE_DATA_NOT_FOUND", message);
    }
}