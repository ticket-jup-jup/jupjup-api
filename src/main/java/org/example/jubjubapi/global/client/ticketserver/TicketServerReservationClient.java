package org.example.jubjubapi.global.client.ticketserver;
// 추후 패키지 이동
public interface TicketServerReservationClient {

    /**
     * 티켓 서버에 임시 예약 요청
     * POST /api/reservations
     */
    Long createTemporaryReservation(Long externalUserId, Long externalTicketId);

    /**
     * 결제 완료 후 티켓 서버의 예약 확정
     * POST /api/reservations/{id}/confirm
     */
    void confirmReservation(Long externalReservationId);

    /**
     * 티켓 서버 예약 취소
     * POST /api/reservations/{id}/cancel
     */
    void cancelReservation(Long externalReservationId);
}
