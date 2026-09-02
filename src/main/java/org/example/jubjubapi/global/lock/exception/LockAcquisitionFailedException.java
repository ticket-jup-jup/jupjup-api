package org.example.jubjubapi.global.lock.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class LockAcquisitionFailedException extends ServiceException {

    public LockAcquisitionFailedException() {
        super(HttpStatus.CONFLICT, "LOCK_ACQUISITION_FAILED", "다른 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.");
    }
}
