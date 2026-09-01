package org.example.jubjubapi.user.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.global.dto.ApiResponse;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
import org.example.jubjubapi.user.dto.ChangePasswordRequest;
import org.example.jubjubapi.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(principal.userId(), request);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Void> withdraw(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        userService.withdraw(principal.userId());
        return ApiResponse.success();
    }
}
