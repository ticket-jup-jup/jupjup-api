package org.example.jubjubapi.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.global.entity.BaseEntity;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_event_user",
                columnNames = {"event_id", "user_id"}))   // 최종 중복 방어선
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(nullable = false, length = 255)
    private String content;

    @Column(name = "canceled_at", nullable = false)
    private LocalDateTime canceledAt;

    private Notification(String eventId, User user, Ticket ticket,
                         Long performanceId, String content, LocalDateTime canceledAt) {
        this.eventId = eventId;
        this.user = user;
        this.ticket = ticket;
        this.performanceId = performanceId;
        this.content = content;
        this.canceledAt = canceledAt;
    }

    public static Notification ticketCanceled(String eventId, User user, Ticket ticket,
                                              Long performanceId, LocalDateTime canceledAt) {
        String content = String.format("[%s] %s %s %s열 %s번 취소표가 나왔어요!",
                ticket.getProgramName(),
                ticket.getStartAt().toLocalDate(),
                ticket.getSeatGrade(),
                ticket.getRowNumber(),
                ticket.getSeatNumber());
        return new Notification(eventId, user, ticket, performanceId, content, canceledAt);
    }
}
