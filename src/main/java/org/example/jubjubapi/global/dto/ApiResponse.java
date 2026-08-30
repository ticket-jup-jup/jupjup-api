package org.example.jubjubapi.global.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final List<T> data;
    private final ErrorResponse error;

    private ApiResponse(boolean success, List<T> data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(List<T> data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, List.of(), null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(
                false,
                null,
                new ErrorResponse(code, message)
        );
    }
}