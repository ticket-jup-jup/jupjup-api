package org.example.jubjubapi.user.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class SamePasswordException extends ServiceException {

    public SamePasswordException() {
        super(HttpStatus.BAD_REQUEST, "SAME_PASSWORD", "새 비밀번호가 현재 비밀번호와 같습니다.");
    }
}
