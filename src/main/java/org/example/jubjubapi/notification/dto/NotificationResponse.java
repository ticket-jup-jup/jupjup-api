package org.example.jubjubapi.notification.dto;

import org.example.jubjubapi.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long ticketId,
        Long performanceId,
        String content,
        LocalDateTime canceledAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                    n.getId(),
                    n.getTicket().getId(),
                    n.getPerformanceId(),
                    n.getContent(),
                    n.getCanceledAt(),
                    n.getCreatedAt()
        );
    }
}
