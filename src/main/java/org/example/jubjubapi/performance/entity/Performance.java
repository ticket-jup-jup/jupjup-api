package org.example.jubjubapi.performance.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.global.entity.BaseEntity;
import org.example.jubjubapi.program.entity.Program;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "performances",
        indexes = {
                @Index(
                        name = "idx_performance_program",
                        columnList = "program_id"
                )
        }
)
public class Performance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "program_id",
            nullable = false,
            updatable = false
    )
    private Program program;

    private Long externalPerformanceId;

    @Column(
            name = "start_at",
            nullable = false
    )
    private LocalDateTime startAt;

    @Column(
            name = "end_at",
            nullable = false
    )
    private LocalDateTime endAt;

    @Column(
            name = "venue",
            nullable = false,
            length = 255
    )
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private PerformanceStatus status;

    private LocalDateTime deletedAt;

    public Performance(
            Program program,
            Long externalPerformanceId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String venue,
            PerformanceStatus status
    ) {
        this.program = Objects.requireNonNull(
                program,
                "프로그램은 필수입니다."
        );

        this.externalPerformanceId = Objects.requireNonNull(
                externalPerformanceId,
                "티켓서버 회차 ID는 필수입니다."
        );

        this.startAt = Objects.requireNonNull(
                startAt,
                "시작 일시는 필수입니다."
        );

        this.endAt = Objects.requireNonNull(
                endAt,
                "종료 일시는 필수입니다."
        );

        this.venue = Objects.requireNonNull(
                venue,
                "장소는 필수입니다."
        );

        this.status = Objects.requireNonNull(
                status,
                "회차 상태는 필수입니다."
        );
    }

    public void update(
            LocalDateTime startAt,
            LocalDateTime endAt,
            String venue,
            PerformanceStatus status
    ) {
        this.startAt = Objects.requireNonNull(startAt);
        this.endAt = Objects.requireNonNull(endAt);
        this.venue = Objects.requireNonNull(venue);
        this.status = Objects.requireNonNull(status);
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
