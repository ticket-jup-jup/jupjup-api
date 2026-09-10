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

import java.math.BigDecimal;
import java.util.Objects;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tickets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ticket_external_id", columnNames = "external_ticket_id"),
        indexes = @Index(name = "idx_ticket_performance", columnList = "performance_id"))
public class Ticket extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "external_ticket_id", nullable = false, updatable = false)
    private Long externalTicketId;//원본 티켓 서버의 ID.

    @Column(name = "performance_id", nullable = false, updatable = false)
    private Long performanceId;


    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status;

    @Builder
    private Ticket(Long externalTicketId, Long performanceId,
                   BigDecimal price, TicketStatus status) {
        this.externalTicketId = Objects.requireNonNull(externalTicketId, "외부 티켓 ID는 필수입니다.");
        this.performanceId = Objects.requireNonNull(performanceId, "회차 ID는 필수입니다.");
        if (price == null || price.signum() < 0) {
            throw new TicketException(TicketErrorCode.INVALID_TICKET_PRICE);
        }
        this.price = price;
        // 이미 판매된 티켓도 동기화하므로 AVAILABLE을 기본값으로 삼지 않는다.
        this.status = Objects.requireNonNull(status, "티켓 상태는 필수입니다.");
    }

    // 호출하는 이벤트 처리 서비스가 중복·순서 검증을 먼저 수행해야 한다.
    public void updateStatus(TicketStatus status) {
        this.status = Objects.requireNonNull(status, "티켓 상태는 필수입니다.");
    }


}
