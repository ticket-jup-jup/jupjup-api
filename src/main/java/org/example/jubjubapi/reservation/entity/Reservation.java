package org.example.jubjubapi.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.global.entity.BaseEntity;
import org.example.jubjubapi.reservation.exception.ReservationAlreadyFinishedException;
import org.example.jubjubapi.reservation.exception.ReservationNotPendingException;
import org.example.jubjubapi.ticket.entity.Ticket;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "reservations",
        indexes = {
                @Index(name = "idx_reservation_user_id", columnList = "user_id"),
                @Index(name = "idx_reservation_status_expires_at", columnList = "status, expires_at")
        }
)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user 파트 완료되면 리팩토링 예정
    @Column(name = "user_id", nullable = false)
    private Long userId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "external_reservation_id")
    private Long externalReservationId; // 티켓서버 예약 id

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Version
    private Long version;

    @Builder
    private Reservation(Long userId, Ticket ticketId, LocalDateTime expiresAt) {
        this.userId = userId;
        this.ticket = ticketId;
        this.status = ReservationStatus.PENDING;
        this.expiresAt = expiresAt;
    }

    public static Reservation create(Long userId, Ticket ticket, LocalDateTime expiresAt) {
        return Reservation.builder()
                .userId(userId)
                .ticketId(ticket)
                .expiresAt(expiresAt)
                .build();
    }

    // 티켓 서버 임시 예약 성공 후 외부 예약 id 연결
    public void linkExternalReservation(Long externalReservationId) {
        this.externalReservationId = externalReservationId;
    }

    // 결제 완료후 예약 확정
    public void confirm() {
        if (this.status != ReservationStatus.PENDING) {
            throw new ReservationNotPendingException();
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    // 만료 시간 초과로 인한 자동 만료 (스케줄러 구현 후 호출 예정)
    public void expire() {
        if (this.status != ReservationStatus.PENDING) {
            throw new ReservationNotPendingException();
        }
        this.status = ReservationStatus.EXPIRED;
    }

    public void cancel() {
        if (this.status == ReservationStatus.EXPIRED || this.status == ReservationStatus.CANCELLED) {
            throw new ReservationAlreadyFinishedException();
        }
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean isExpired(LocalDateTime now) {
        return this.expiresAt != null && this.expiresAt.isBefore(now);
    }

    // user 파트 완료되면 리팩토링 예정 (this.user.getId())
    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }
}
