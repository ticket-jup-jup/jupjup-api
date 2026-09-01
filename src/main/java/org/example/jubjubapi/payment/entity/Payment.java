package org.example.jubjubapi.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.payment.exception.PaymentNotCompletedException;
import org.example.jubjubapi.payment.exception.PaymentNotPendingException;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "payments",
        indexes = {
                // 결제 시도마다 새 레코드 남김. 예약당 여러 건 존재 가능
                @Index(name = "idx_payment_reservation_id_status", columnList = "reservation_id, status")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "amount", nullable = false, length = 20)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreatedDate
    @Column(name = "creadted_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private Payment(Reservation reservation, BigDecimal amount, PaymentMethod paymentMethod) {
        this.reservation = reservation;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
    }

    public static Payment create(Reservation reservation, BigDecimal amount, PaymentMethod paymentMethod) {
        return Payment.builder()
                .reservation(reservation)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .build();
    }

    // 결제완료처리
    public void complete(LocalDateTime now) {
        if(this.status != PaymentStatus.PENDING) {
            throw new PaymentNotPendingException();
        }
        this.status = PaymentStatus.COMPLETED;
        this.paidAt = now;
    }

    // 결제실패처리
    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new PaymentNotPendingException();
        }
        this.status = PaymentStatus.FAILED;
    }

    // 결제취소처리
    public void cancel() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new PaymentNotCompletedException();
        }
        this.status = PaymentStatus.CANCELLED;
    }
}
