package org.example.jubjubapi.ticket.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class TicketServerClient {

    private final RestClient restClient;
    // 티켓서버 연동 후 제거 예정
    private final AtomicLong temporarySequence = new AtomicLong(System.currentTimeMillis());

    public TicketServerClient(
            RestClient.Builder restClientBuilder,
            @Value("${ticket-server.url}") String url,
            @Value("${ticket-server.api-key}") String apiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(url)
                .defaultHeader("X-API-KEY", apiKey)
                .build();
    }

    // 티켓 서버 API 구현 후 HTTP 호출로 교체 예정
    public Long createTemporaryReservation(Long externalUserId, Long externalTicketId) {
        long externalReservationId = temporarySequence.getAndIncrement();
        log.info("[임시] 임시예약 요청 - userId={}, ticketId={} -> reservationId={}", externalUserId, externalTicketId, externalReservationId);
        return externalReservationId;
    }

    // 티켓 서버 API 구현 후 HTTP 호출로 교체 예정
    public void confirmReservation(Long externalReservationId) {
        log.info("[임시] 예약 확정 - reservationId={}", externalReservationId);
    }

    // 티켓 서버 API 구현 후 HTTP 호출로 교체 예정
    public void cancelReservation(Long externalReservationId) {
        log.info("[임시] 예약 취소 - reservationId={}", externalReservationId);
    }
}