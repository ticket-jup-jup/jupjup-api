package org.example.jubjubapi.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.global.lock.config.DistributedLockExecutor;
import org.example.jubjubapi.reservation.dto.request.ReservationCreateRequest;
import org.example.jubjubapi.reservation.dto.response.ReservationCreateResponse;
import org.springframework.stereotype.Service;

/**
 * 락 담당 클래스
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final String LOCK_KEY_PREFIX = "lock:ticket";

    private final DistributedLockExecutor lockExecutor;
    private final ReservationTransactionService reservationTransactionService;

    public ReservationCreateResponse reserve(Long userId, ReservationCreateRequest request) {
        String lockKey = LOCK_KEY_PREFIX + request.getTicketId();

        return lockExecutor.execute(lockKey,
                () -> reservationTransactionService.reserve(userId, request));
    }
}
