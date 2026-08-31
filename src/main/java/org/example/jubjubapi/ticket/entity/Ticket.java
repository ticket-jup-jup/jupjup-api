package org.example.jubjubapi.ticket.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.global.entity.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ticket",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ticket_external_id", columnNames = "external_ticket_id"),
        indexes = @Index(name = "idx_ticket_performance", columnList = "performance_id"))
public class Ticket extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name="external_ticket_id", nullable = false,updatable = false)
    private Long externalTicketId;//원본 티켓 서버의 ID.

    @Column(name = "performance_id", nullable = false, updatable = false)
    private Long performanceId;

    @Column(name = "program_name", nullable = false, length = 255)
    private String programName;
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "venue", nullable = false, length = 255)
    private String venue;

    @Column(name = "seat_grade", nullable = false, length = 20)
    private String seatGrade;

    @Column(name = "section", length = 50)
    private String section;

    // ROW_NUMBER는 MySQL 예약어이므로 Hibernate 식별자 인용을 사용한다.
    @Column(name = "`row_number`", length = 20)
    private String rowNumber;

    @Column(name = "seat_number", length = 20)
    private String seatNumber;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status;

    @Builder
    private Ticket(Long externalTicketId, Long performanceId, String programName,
                   LocalDateTime startAt, String venue, String seatGrade,
                   String section, String rowNumber, String seatNumber,
                   BigDecimal price, TicketStatus status) {
        this.externalTicketId = Objects.requireNonNull(externalTicketId, "외부 티켓 ID는 필수입니다.");
        this.performanceId = Objects.requireNonNull(performanceId, "회차 ID는 필수입니다.");
        this.programName = Objects.requireNonNull(programName, "프로그램명은 필수입니다.");
        this.startAt = Objects.requireNonNull(startAt, "시작 일시는 필수입니다.");
        this.venue = Objects.requireNonNull(venue, "장소는 필수입니다.");
        this.seatGrade = seatGrade;
        this.section = section;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.price = Objects.requireNonNull(price, "가격은 필수입니다.");
        if (price.signum() < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        // 이미 판매된 티켓도 동기화하므로 AVAILABLE을 기본값으로 삼지 않는다.
        this.status = Objects.requireNonNull(status, "티켓 상태는 필수입니다.");
    }

    // 호출하는 이벤트 처리 서비스가 중복·순서 검증을 먼저 수행해야 한다.
    public void updateStatus(TicketStatus status) {
        this.status = Objects.requireNonNull(status, "티켓 상태는 필수입니다.");
    }





}
