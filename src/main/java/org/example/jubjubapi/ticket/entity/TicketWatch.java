package org.example.jubjubapi.ticket.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.global.entity.BaseEntity;
import org.example.jubjubapi.user.entity.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ticket_watch",
            uniqueConstraints = @UniqueConstraint(name = "uk_watch_user_ticket",
                columnNames = {"user_id","ticket_id"}),
    indexes = @Index(name = "idx_watch_ticket_status", columnList =
    "ticket_id, status"))
public class TicketWatch extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id",nullable = false,updatable = false)
    private User user;

    //줍줍 서버의 Ticket.id 참조.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false,length = 20)
    private TicketWatchStatus status;

    private TicketWatch(User user, Ticket ticket) {
        this.user = Objects.requireNonNull(user, "사용자는 필수입니다.");
        this.ticket = Objects.requireNonNull(ticket, "티켓은 필수입니다.");
        this.status = TicketWatchStatus.ACTIVE;
    }

    public static TicketWatch create(User user, Ticket ticket) {
        return new TicketWatch(user, ticket);
    }

    public void activate() {
        this.status = TicketWatchStatus.ACTIVE;
    }

    // 알림 이력이 참조하는 구독 행은 보존.
    public void deactivate() {
        this.status = TicketWatchStatus.INACTIVE;
    }

    public boolean isActive() {
        return this.status == TicketWatchStatus.ACTIVE;
    }

    public boolean isOwnedBy(Long userId) {
        return userId != null && userId.equals(user.getId());
    }


}
