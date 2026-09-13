package org.example.jubjubapi.seat.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class SeatNotFoundException extends ServiceException {

    public SeatNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "SEAT_NOT_FOUND", message);
    }
}
