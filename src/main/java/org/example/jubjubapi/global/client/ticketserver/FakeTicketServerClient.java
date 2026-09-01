package org.example.jubjubapi.global.client.ticketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 티켓서버 연동 전 임시 FAKE 구현체
 */
@Slf4j
@Component
@Profile("!prod")
public class FakeTicketServerClient implements TicketServerClient {

    private final AtomicLong sequence = new AtomicLong(1L);


    @Override
    public Long createTemporaryReservation(Long externalUserId, Long externalTicketId) {
        long externalReservationId = sequence.getAndIncrement();
        log.info("[FAKE] 임시예약 요청 - userId={}, ticketId={} -> reservationId={}",
                externalUserId, externalTicketId, externalReservationId);
        return externalReservationId;
    }

    @Override
    public void confirmReservation(Long externalReservationId) {
        log.info("[FAKE] 예약 확정 - reservationId={}", externalReservationId);
    }

    @Override
    public void cancelReservation(Long externalReservationId) {
        log.info("[FAKE] 예약 취소 - reservationId={}", externalReservationId);
    }
}
