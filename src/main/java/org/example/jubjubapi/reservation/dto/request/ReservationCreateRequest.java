package org.example.jubjubapi.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationCreateRequest {

    @NotNull(message = "티켓 ID는 필수입니다.")
    private Long ticketId;

    public ReservationCreateRequest(Long ticketId) {
        this.ticketId = ticketId;
    }
}
