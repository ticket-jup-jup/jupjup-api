package org.example.jubjubapi.ticket.performance.entity;

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
@Table(
        name = "performance_watch",
        uniqueConstraints = @UniqueConstraint(//중복구독방지
                name = "uk_performance_watch_user_performance",
                columnNames = {"user_id", "performance_id"}
        ),
        indexes = @Index(
                name = "idx_performance_watch_performance_status",
                columnList = "performance_id, status"
        )
)
public class PerformanceWatch extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "performance_id",
            nullable = false,
            updatable = false
    )
    private Performance performance;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private PerformanceWatchStatus status;

    private PerformanceWatch(
            User user,
            Performance performance
    ) {
        this.user = Objects.requireNonNull(
                user,
                "사용자는 필수입니다."
        );

        this.performance = Objects.requireNonNull(
                performance,
                "회차는 필수입니다."
        );

        this.status = PerformanceWatchStatus.ACTIVE;
    }

    public static PerformanceWatch create(
            User user,
            Performance performance
    ) {
        return new PerformanceWatch(user, performance);
    }

    public void activate() {
        this.status = PerformanceWatchStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = PerformanceWatchStatus.INACTIVE;
    }

    public boolean isActive() {
        return this.status == PerformanceWatchStatus.ACTIVE;
    }

    public boolean isOwnedBy(Long userId) {
        return userId != null
                && userId.equals(this.user.getId());
    }

}
