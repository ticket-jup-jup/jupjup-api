package org.example.jubjubapi.reservation.entity;

public enum ReservationStatus {
    PENDING, // 임시예약 (결제 대기)
    CONFIRMED, // 예약확정 (결제 완료)
    EXPIRED, // 만료 (결제 시간 초과)
    CANCELLED // 취소
}
