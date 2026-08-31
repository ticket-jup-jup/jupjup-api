package org.example.jubjubapi.reservation.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class ReservationNotPendingException extends ServiceException {


    public ReservationNotPendingException() {
        super(HttpStatus.BAD_REQUEST, "RESERVATION_NOT_PENDING", "결제 대기 중인 예약이 아닙니다.");
    }
}
