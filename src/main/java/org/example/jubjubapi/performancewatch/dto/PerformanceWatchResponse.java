package org.example.jubjubapi.performancewatch.dto;


import lombok.Getter;
import org.example.jubjubapi.performancewatch.entity.PerformanceWatch;
import org.example.jubjubapi.performancewatch.entity.PerformanceWatchStatus;

import java.time.LocalDateTime;

@Getter
public class PerformanceWatchResponse {
    private final Long id;
    private final Long performanceId;
    private final PerformanceWatchStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PerformanceWatchResponse(Long id, Long ticketId, PerformanceWatchStatus status,
                                    LocalDateTime createdAt,
                                    LocalDateTime updatedAt) {
        this.id = id;
        this.performanceId = ticketId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PerformanceWatchResponse from(PerformanceWatch watch) {
        return new PerformanceWatchResponse(watch.getId(), watch.getPerformance().getId(),
                watch.getStatus(),
                watch.getCreatedAt(), watch.getUpdatedAt());
    }
}