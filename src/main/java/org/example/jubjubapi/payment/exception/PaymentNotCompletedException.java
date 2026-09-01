package org.example.jubjubapi.payment.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class PaymentNotCompletedException extends ServiceException {
    public PaymentNotCompletedException() {
        super(HttpStatus.BAD_REQUEST, "PAYMENT_NOT_COMPLETED", "완료된 결제만 취소할 수 있습니다.");
    }
}
