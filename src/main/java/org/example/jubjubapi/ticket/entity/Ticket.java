package org.example.jubjubapi.ticket.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.global.entity.BaseEntity;
import org.example.jubjubapi.ticket.exception.TicketErrorCode;
import org.example.jubjubapi.ticket.exception.TicketException;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.example.jubjubapi.ticket.performance.entity.Performance;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performance_id", nullable = false, updatable = false)
    private Performance performance;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status;

    @Column(name = "program_name", nullable = false, length = 255)
    private String programName;

    @Column(name = "seat_grade", nullable = false, length = 20)
    private String seatGrade;

    @Column(name = "section", length = 50)
    private String section;

    @Column(name = "`row_number`", length = 20)
    private String rowNumber;

    @Column(name = "seat_number", length = 20)
    private String seatNumber;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;


    @Builder
    private Ticket(Long externalTicketId, Performance performance,
                   String programName, String seatGrade, String section,
                   String rowNumber, String seatNumber, BigDecimal price,
                   TicketStatus status) {
        this.externalTicketId = Objects.requireNonNull(externalTicketId, "외부 티켓 ID는 필수입니다.");
        this.performance = Objects.requireNonNull(performance, "회차 ID는 필수입니다.");
        this.programName = Objects.requireNonNull(programName, "프로그램명은 필수입니다.");
        this.seatGrade = seatGrade;
        this.section = section;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        if (price == null || price.signum() < 0) {
            throw new TicketException(TicketErrorCode.INVALID_TICKET_PRICE);
        }
        this.price = price;
        this.status = Objects.requireNonNull(status, "티켓 상태는 필수입니다.");
    }

    // 호출하는 이벤트 처리 서비스가 중복·순서 검증을 먼저 수행해야 한다.
    public void updateStatus(TicketStatus status) {
        this.status = Objects.requireNonNull(status, "티켓 상태는 필수입니다.");
    }


    public Long getPerformanceId() {
        return performance.getId();
    }

    public LocalDateTime getStartAt() {
        return performance.getStartAt();
    }

    public String getVenue() {
        return performance.getVenue();
    }



}
