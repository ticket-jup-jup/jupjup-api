package org.example.jubjubapi.reservation.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class ReservationAccessDeniedException extends ServiceException {

    public ReservationAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "RESERVATION_ACCESS_DENIED", "본인의 예약만 조회할 수 있습니다.");

    }
}
