package org.example.jubjubapi.reservation.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class ReservationNotFoundException extends ServiceException {

    public ReservationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "예약을 찾을 수 없습니다.");
    }
}
