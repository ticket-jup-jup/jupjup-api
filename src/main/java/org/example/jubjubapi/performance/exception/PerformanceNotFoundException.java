package org.example.jubjubapi.performance.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class PerformanceNotFoundException extends ServiceException {

    public PerformanceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "PERFORMANCE_NOT_FOUND", message);
    }
}
