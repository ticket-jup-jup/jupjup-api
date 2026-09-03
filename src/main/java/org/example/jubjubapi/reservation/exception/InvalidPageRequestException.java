package org.example.jubjubapi.reservation.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class InvalidPageRequestException extends ServiceException {
    public InvalidPageRequestException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_PAGE_REQUEST", "페이지 요청이 올바르지 않습니다.");

    }
}
