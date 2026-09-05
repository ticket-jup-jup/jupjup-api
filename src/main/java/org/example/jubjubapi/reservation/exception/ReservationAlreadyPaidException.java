package org.example.jubjubapi.reservation.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class ReservationAlreadyPaidException extends ServiceException {

    public ReservationAlreadyPaidException() {
        super(HttpStatus.CONFLICT, "RESERVATION_ALREADY_PAID", "결제가 완료된 예약입니다. 결제 취소를 이용해주세요.");
    }
}
