package org.example.jubjubapi.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.global.lock.config.DistributedLockExecutor;
import org.example.jubjubapi.reservation.dto.request.ReservationCreateRequest;
import org.example.jubjubapi.reservation.dto.response.ReservationCreateResponse;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.entity.ReservationStatus;
import org.example.jubjubapi.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 락 담당 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final String LOCK_KEY_PREFIX = "lock:ticket:";

    private final DistributedLockExecutor lockExecutor;
    private final ReservationTransactionService reservationTransactionService;
    private final ReservationRepository reservationRepository;

    public ReservationCreateResponse reserve(Long userId, ReservationCreateRequest request) {
        String lockKey = LOCK_KEY_PREFIX + request.getTicketId();

        return lockExecutor.execute(lockKey,
                () -> reservationTransactionService.reserve(userId, request));
    }

    @Transactional
    public void expireReservations() {
        log.info("결제 만료된 예약 처리 시작");

        List<Reservation> reservations =
                reservationRepository.findAllByStatusAndExpiresAtLessThanEqual(
                        ReservationStatus.PENDING,
                        LocalDateTime.now()
                );

        log.info("결제 만료 대상 예약 수: {}", reservations.size());

        for (Reservation reservation : reservations) {
            reservation.expire();
        }

        log.info("결제 만료된 예약 처리 완료");
    }
}
