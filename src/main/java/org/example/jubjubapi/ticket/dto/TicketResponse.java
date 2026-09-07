package org.example.jubjubapi.ticket.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.performance.entity.Performance;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
public class TicketResponse {
    private final Long id;
    private final Long externalTicketId;
    private final Long performanceId;
    private final Long programId;
    private final String programName;

    private final LocalDateTime startAt;
    private final String venue;
    private final String seatGrade;
    private final String section;
    private final String rowNumber;
    private final String seatNumber;
    private final BigDecimal price;

    private final TicketStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public TicketResponse(Long id, Long externalTicketId, Long performanceId,
                           Long programId, String programName,
                           LocalDateTime startAt, String venue,
                           String seatGrade, String section, String rowNumber,
                           String seatNumber, BigDecimal price, TicketStatus status,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.externalTicketId = externalTicketId;
        this.performanceId = performanceId;
        this.programId = programId;
        this.programName = programName;
        this.startAt = startAt;
        this.venue = venue;
        this.seatGrade = seatGrade;
        this.section = section;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TicketResponse from(Ticket ticket) {
        Performance performance = ticket.getPerformance();

        return new TicketResponse(
                ticket.getId(),
                ticket.getExternalTicketId(),
                performance.getId(),
                performance.getProgramId(),
                ticket.getProgramName(),
                performance.getStartAt(),
                performance.getVenue(),
                ticket.getSeatGrade(),
                ticket.getSection(),
                ticket.getRowNumber(),
                ticket.getSeatNumber(),
                ticket.getPrice(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}
