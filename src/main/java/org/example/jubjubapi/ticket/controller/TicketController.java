package org.example.jubjubapi.ticket.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.global.dto.ApiResponse;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
import org.example.jubjubapi.ticket.dto.TicketResponse;
import org.example.jubjubapi.ticket.dto.TicketWatchCreateRequest;
import org.example.jubjubapi.ticket.dto.TicketWatchResponse;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.entity.TicketWatchStatus;
import org.example.jubjubapi.ticket.exception.TicketErrorCode;
import org.example.jubjubapi.ticket.exception.TicketException;
import org.example.jubjubapi.ticket.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    //티켓목록조회
    @GetMapping("/tickets")
    public ApiResponse<TicketResponse> getTickets(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(required = false) Long performanceId,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireUserId(principal);
        return ApiResponse.success(ticketService.getTickets(performanceId, status, page, size));
    }
    //티켓단건조회
    @GetMapping("/tickets/{ticketId}")
    public ApiResponse<TicketResponse> getTicket(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long ticketId) {
        requireUserId(principal);
        // ApiResponse가 data배열로 반환하도록 설계됨. 단건이지만 List.of() 사용.
        return ApiResponse.success(List.of(ticketService.getTicket(ticketId)));
    }
    //티켓자체삭제(관리자용)
    @DeleteMapping("/tickets/{ticketId}")
    public ResponseEntity<Void> deleteTicket(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long ticketId) {
        requireUserId(principal);
        // 현재 Role에는 USER만 있다. 인증 담당자가 ADMIN을 도입하기 전에는
        // 항상 거절한다. 메서드 보안 설정 누락 시에도 일반 사용자에게 열리지 않는다.
        if (principal.role() == null || !"ROLE_ADMIN".equals(principal.role().authority())) {
            throw new TicketException(TicketErrorCode.ADMIN_REQUIRED);
        }
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.noContent().build();
    }
    //취소표 알림 구독 생성
    @PostMapping("/ticket-watches")
    public ResponseEntity<ApiResponse<TicketWatchResponse>> createWatch(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody TicketWatchCreateRequest request) {
        TicketWatchResponse response =
                ticketService.createWatch(requireUserId(principal), request.getTicketId());
        return ResponseEntity.created(URI.create("/api/ticket-watches/" + response.getId()))
                .body(ApiResponse.success(List.of(response)));
    }
    //사용자의 알림 구독 목록 조회
    @GetMapping("/ticket-watches")
    public ApiResponse<TicketWatchResponse> getMyWatches(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(defaultValue = "ACTIVE") TicketWatchStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(
                ticketService.getMyWatches(requireUserId(principal), status, page, size));
    }
    //취소표 알림 구독 해제
    @DeleteMapping("/ticket-watches/{watchId}")
    public ResponseEntity<Void> deactivateWatch(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long watchId) {
        ticketService.deactivateWatch(requireUserId(principal), watchId);
        return ResponseEntity.noContent().build();
    }

    // 요청 DTO 검증 실패 처리
    //기존 공통 예외 처리기는 ServiceException만 다루므로 이 DTO 오류를 변환한다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_REQUEST", message));
    }

    //JWT 사용자 정보 검사
    private Long requireUserId(JwtUserPrincipal principal) {
        if (principal == null || principal.userId() == null || principal.userId() <= 0) {
            throw new TicketException(TicketErrorCode.AUTHENTICATION_REQUIRED);
        }
        return principal.userId();
    }
}

