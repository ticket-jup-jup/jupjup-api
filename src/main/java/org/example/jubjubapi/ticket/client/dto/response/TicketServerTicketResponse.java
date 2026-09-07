package org.example.jubjubapi.ticket.client.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.ticket.entity.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TicketServerTicketResponse {

    private Long id;
    private Long performanceId;
    private String programName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String venue;
    private String section;
    private String rowNumber;
    private Integer seatNumber;
    private BigDecimal price;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}