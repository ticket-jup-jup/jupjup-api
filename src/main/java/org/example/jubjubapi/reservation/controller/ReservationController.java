package org.example.jubjubapi.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.global.dto.ApiResponse;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
import org.example.jubjubapi.reservation.dto.request.ReservationCreateRequest;
import org.example.jubjubapi.reservation.dto.response.ReservationCreateResponse;
import org.example.jubjubapi.reservation.service.ReservationService;
import org.example.jubjubapi.reservation.service.ReservationTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    // 취소표 임시 예약
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationCreateResponse>> reserve(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationCreateResponse response = reservationService.reserve(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(List.of(response)));
    }
}
