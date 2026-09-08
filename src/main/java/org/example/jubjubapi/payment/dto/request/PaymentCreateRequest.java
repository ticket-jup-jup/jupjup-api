package org.example.jubjubapi.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.payment.entity.PaymentMethod;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCreateRequest {

    @NotNull(message = "예약 ID는 필수입니다.")
    private Long reservationId;
    @NotNull(message = "결제 수단은 필수입니다.")
    private PaymentMethod paymentMethod;

    public PaymentCreateRequest(Long reservationId, PaymentMethod paymentMethod) {
        this.reservationId = reservationId;
        this.paymentMethod = paymentMethod;
    }
}
