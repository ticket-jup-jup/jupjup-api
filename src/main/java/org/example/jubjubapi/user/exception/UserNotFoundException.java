package org.example.jubjubapi.user.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

/**
 * 토큰은 유효한데 DB 에 사용자가 없거나 이미 탈퇴한 경우.
 * (탈퇴 후에도 토큰은 만료 전까지 유효하므로 이 케이스가 실제로 발생할 수 있다)
 */
public class UserNotFoundException extends ServiceException {

    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
    }
}

