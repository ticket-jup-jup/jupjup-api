package org.example.jubjubapi.payment.service;

import org.example.jubjubapi.payment.dto.request.PaymentCreateRequest;
import org.example.jubjubapi.payment.entity.PaymentMethod;
import org.example.jubjubapi.payment.repository.PaymentRepository;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.exception.ReservationAccessDeniedException;
import org.example.jubjubapi.reservation.exception.ReservationNotFoundException;
import org.example.jubjubapi.reservation.repository.ReservationRepository;
import org.example.jubjubapi.ticket.client.TicketServerClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("결제 서비스 단위 테스트")
public class PaymentTransactionServiceTest {

    @InjectMocks
    private PaymentTransactionService paymentTransactionService;

    @Mock
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("존재하지 않는 예약이면 예외가 발생한다.")
    void 없는_예약() {
        //given
        given(reservationRepository.findByIdWithTicket(anyLong()))
                .willReturn(Optional.empty());

        //when,then
        assertThatThrownBy(() -> paymentTransactionService.pay(1L, new PaymentCreateRequest(999L, PaymentMethod.CARD)))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @DisplayName("다른 사용자의 예약은 결제할 수 없다.")
    void 다른_사용자_예약_결제() {
        //given
        Reservation reservation = mock(Reservation.class);
        given(reservation.isOwnedBy(1L)).willReturn(false);
        given(reservationRepository.findByIdWithTicket(anyLong()))
                .willReturn(Optional.of(reservation));

        //when,then
        assertThatThrownBy(() -> paymentTransactionService.pay(1L, new PaymentCreateRequest(1L, PaymentMethod.CARD)))
                .isInstanceOf(ReservationAccessDeniedException.class);
    }
}
