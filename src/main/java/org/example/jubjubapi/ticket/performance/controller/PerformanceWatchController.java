package org.example.jubjubapi.ticket.performance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.global.dto.ApiResponse;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
import org.example.jubjubapi.ticket.exception.TicketErrorCode;
import org.example.jubjubapi.ticket.exception.TicketException;
import org.example.jubjubapi.ticket.performance.dto.PerformanceWatchCreateRequest;
import org.example.jubjubapi.ticket.performance.dto.PerformanceWatchResponse;
import org.example.jubjubapi.ticket.performance.entity.PerformanceWatchStatus;
import org.example.jubjubapi.ticket.performance.service.PerformanceWatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/performance-watches")
public class PerformanceWatchController {

    private final PerformanceWatchService performanceWatchService;

    // 회차 취소표 알림 구독 생성
    @PostMapping
    public ResponseEntity<ApiResponse<PerformanceWatchResponse>> createWatch(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody PerformanceWatchCreateRequest request
    ) {
        PerformanceWatchResponse response =
                performanceWatchService.createWatch(
                        requireUserId(principal),
                        request.getPerformanceId()
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/performance-watches/"
                                        + response.getId()
                        )
                )
                .body(
                        ApiResponse.success(
                                List.of(response)
                        )
                );
    }

    // 내 회차 알림 구독 목록 조회
    @GetMapping
    public ApiResponse<PerformanceWatchResponse> getMyWatches(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(defaultValue = "ACTIVE")
            PerformanceWatchStatus status,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size
    ) {
        return ApiResponse.success(
                performanceWatchService.getMyWatches(
                        requireUserId(principal),
                        status,
                        page,
                        size
                )
        );
    }

    // 회차 알림 구독 해제
    @DeleteMapping("/{watchId}")
    public ResponseEntity<Void> deactivateWatch(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long watchId
    ) {
        performanceWatchService.deactivateWatch(
                requireUserId(principal),
                watchId
        );

        return ResponseEntity.noContent().build();
    }

    private Long requireUserId(
            JwtUserPrincipal principal
    ) {
        if (
                principal == null ||
                        principal.userId() == null ||
                        principal.userId() <= 0
        ) {
            throw new TicketException(
                    TicketErrorCode.AUTHENTICATION_REQUIRED
            );
        }

        return principal.userId();
    }
}