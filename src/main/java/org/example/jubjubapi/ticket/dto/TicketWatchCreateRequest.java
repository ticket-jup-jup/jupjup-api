package org.example.jubjubapi.ticket.dto;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;


//userId는 요청으로 받지 않고 인증된 사용자의 ID 사용
@Getter
@NoArgsConstructor
public class TicketWatchCreateRequest {
    @NotNull( message= "티켓 ID는 필수입니다.")
    @Positive(message = "티켓 ID는 1 이상이어야 합니다.")
    private Long ticketId;

    public TicketWatchCreateRequest(Long ticketId){
        this.ticketId = ticketId;
    }
}
