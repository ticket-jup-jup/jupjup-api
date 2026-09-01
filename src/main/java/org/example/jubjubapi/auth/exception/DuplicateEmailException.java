package org.example.jubjubapi.auth.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends ServiceException {
    public DuplicateEmailException() {
        super(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.");
    }
}
