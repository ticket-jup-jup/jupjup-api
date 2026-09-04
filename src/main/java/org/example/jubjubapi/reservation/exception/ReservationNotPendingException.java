package org.example.jubjubapi.reservation.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class ReservationNotPendingException extends ServiceException {


    public ReservationNotPendingException(String message) {
        super(HttpStatus.CONFLICT, "RESERVATION_NOT_PENDING", message);
    }
}
