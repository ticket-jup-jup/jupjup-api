package org.example.jubjubapi.performance.dto;

import lombok.Getter;
import org.example.jubjubapi.performance.entity.PerformanceStatus;

import java.time.LocalDateTime;

@Getter
public class TicketServerPerformance {

    private final Long id;
    private final Long programId;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final String venue;
    private final PerformanceStatus status;

    public TicketServerPerformance(Long id, Long programId, LocalDateTime startAt, LocalDateTime endAt, String venue, PerformanceStatus status) {
        this.id = id;
        this.programId = programId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.venue = venue;
        this.status = status;
    }
}