package org.example.jubjubapi.reservation.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class ReservationAccessDeniedException extends ServiceException {

    public ReservationAccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, "RESERVATION_ACCESS_DENIED", message);

    }
}
