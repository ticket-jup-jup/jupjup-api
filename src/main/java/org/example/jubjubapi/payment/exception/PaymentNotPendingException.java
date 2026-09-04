package org.example.jubjubapi.payment.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class PaymentNotPendingException extends ServiceException {

    public PaymentNotPendingException() {
        super(HttpStatus.CONFLICT, "PAYMENT_NOT_PENDING", "결제 대기 중인 건이 아닙니다.");
    }
}
