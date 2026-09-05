package org.example.jubjubapi.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.global.dto.ApiResponse;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
import org.example.jubjubapi.payment.dto.response.PaymentGetResponse;
import org.example.jubjubapi.payment.dto.request.PaymentCreateRequest;
import org.example.jubjubapi.payment.dto.response.PaymentCreateResponse;
import org.example.jubjubapi.payment.service.PaymentService;
import org.example.jubjubapi.payment.service.PaymentTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentTransactionService paymentTransactionService;

    // 결제요청
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> pay(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        PaymentCreateResponse pay = paymentService.pay(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(List.of(pay)));
    }

    // 결제단건조회
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentGetResponse>> getOnePayment(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long paymentId
    ) {
        PaymentGetResponse response = paymentTransactionService.getOnePayment(principal.userId(), paymentId);
        return ResponseEntity.ok(ApiResponse.success(List.of(response)));
    }

    // 결제전체조회
    @GetMapping
    public ResponseEntity<ApiResponse<PaymentGetResponse>> getAllPayment(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<PaymentGetResponse> response = paymentTransactionService.getAllPayment(principal.userId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
