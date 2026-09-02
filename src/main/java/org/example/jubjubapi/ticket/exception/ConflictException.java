package org.example.jubjubapi.ticket.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class ConflictException extends ServiceException {
    public ConflictException(
                             String code,
                             String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}
