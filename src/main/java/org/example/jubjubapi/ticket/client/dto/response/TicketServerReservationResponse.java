package org.example.jubjubapi.ticket.client.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class TicketServerReservationResponse {

    private boolean success;
    private List<ReservationData> data;
    private ErrorData error;

    @Getter
    @NoArgsConstructor
    public static class ReservationData {
        private ReservationInfo reservation;
    }

    @Getter
    @NoArgsConstructor
    public static class ReservationInfo {
        private Long reservationId;
        private Long userId;
        private Long ticketId;
        private String status;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @NoArgsConstructor
    public static class ErrorData {
        private String code;
        private String message;
    }

    // 응답값 검증 메서드
    public boolean isValid() {
        return success && findReservationId() != null;
    }

    // 티켓서버에서 발급된 예약 id 반환 메서드
    public Long extractReservationId() {
        return findReservationId();
    }

    private Long findReservationId() {
        if (data == null || data.isEmpty()) {
            return null;
        }

        ReservationInfo reservation = data.get(0).getReservation();
        return (reservation == null) ? null : reservation.getReservationId();
    }
}
