package org.example.jubjubapi.ticketserver.controller;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.global.dto.ApiResponse;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
import org.example.jubjubapi.ticketserver.dto.LinkTicketServerAccountRequest;
import org.example.jubjubapi.ticketserver.dto.TicketServerAccountResponse;
import org.example.jubjubapi.ticketserver.service.TicketServerAccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/ticket-server-account")
@RequiredArgsConstructor
public class TicketServerAccountController {
    private final TicketServerAccountService ticketServerAccountService;

    @PutMapping
    public ApiResponse<TicketServerAccountResponse> link(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody LinkTicketServerAccountRequest request
    ) {
        TicketServerAccountResponse response = ticketServerAccountService.link(principal.userId(), request);
        return ApiResponse.success(List.of(response));
    }
}
