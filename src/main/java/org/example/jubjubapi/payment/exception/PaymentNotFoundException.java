package org.example.jubjubapi.payment.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends ServiceException {

    public PaymentNotFoundException() {
        super(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "해당 결제건은 존재하지 않습니다");
    }
}
