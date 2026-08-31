package org.example.jubjubapi.reservation.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class ReservationAlreadyFinishedException extends ServiceException {

    public ReservationAlreadyFinishedException() {
        super(HttpStatus.BAD_REQUEST, "RESERVATION_ALREADY_FINISHED", "이미 종료된 예약입니다.");
    }
}
