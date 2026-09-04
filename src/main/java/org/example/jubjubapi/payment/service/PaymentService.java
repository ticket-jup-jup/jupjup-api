package org.example.jubjubapi.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.global.lock.config.DistributedLockExecutor;
import org.example.jubjubapi.payment.dto.request.PaymentCreateRequest;
import org.example.jubjubapi.payment.dto.response.PaymentCreateResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String LOCK_KEY_PREFIX = "lock:reservation:";

    private final DistributedLockExecutor lockExecutor;
    private final PaymentTransactionService paymentTransactionService;

    public PaymentCreateResponse pay(Long userId, PaymentCreateRequest request) {
        String lockKey = LOCK_KEY_PREFIX + request.getReservationId();
        return lockExecutor.execute(lockKey, () -> paymentTransactionService.pay(userId, request));
    }
}
