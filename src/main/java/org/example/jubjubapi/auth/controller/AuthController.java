package org.example.jubjubapi.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.auth.dto.SigninRequest;
import org.example.jubjubapi.auth.dto.SigninResponse;
import org.example.jubjubapi.auth.dto.SignupRequest;
import org.example.jubjubapi.auth.service.AuthService;
import org.example.jubjubapi.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ApiResponse.success();
    }

    @PostMapping("/signin")
    public ApiResponse<SigninResponse> signin(@Valid @RequestBody SigninRequest request) {
        SigninResponse response = authService.signin(request);
        return ApiResponse.success(List.of(response));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }
}




