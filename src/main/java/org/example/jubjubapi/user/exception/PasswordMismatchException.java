package org.example.jubjubapi.user.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class PasswordMismatchException extends ServiceException {

    public PasswordMismatchException() {
        super(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "현재 비밀번호가 일치하지 않습니다.");
    }
}