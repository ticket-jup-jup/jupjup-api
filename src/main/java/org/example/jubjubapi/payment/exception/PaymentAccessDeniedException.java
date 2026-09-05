package org.example.jubjubapi.payment.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class PaymentAccessDeniedException extends ServiceException {
    public PaymentAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "PAYMENT_ACCESS_DENIED", "본인 결제 내역만 조회 가능합니다.");
    }
}
