package org.example.jubjubapi.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.global.dto.ApiResponse;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
import org.example.jubjubapi.payment.dto.request.PaymentCreateRequest;
import org.example.jubjubapi.payment.dto.response.PaymentCreateResponse;
import org.example.jubjubapi.payment.service.PaymentService;
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
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    // 결제요청
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> pay(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        PaymentCreateResponse pay = paymentService.pay(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(List.of(pay)));
    }
}
