package org.example.jubjubapi.payment.entity;

public enum PaymentStatus {
    PENDING, // 결제대기
    COMPLETED, // 결제완료
    FAILED, // 결제실패
    REFUNDED // 환불(예약 취소)
}
