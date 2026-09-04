package org.example.jubjubapi.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.payment.dto.request.PaymentCreateRequest;
import org.example.jubjubapi.payment.dto.response.PaymentCreateResponse;
import org.example.jubjubapi.payment.entity.Payment;
import org.example.jubjubapi.payment.repository.PaymentRepository;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.exception.ReservationAccessDeniedException;
import org.example.jubjubapi.reservation.exception.ReservationNotFoundException;
import org.example.jubjubapi.reservation.repository.ReservationRepository;
import org.example.jubjubapi.ticket.client.TicketServerClient;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final TicketServerClient ticketServerClient;

    public PaymentCreateResponse pay(Long userId, PaymentCreateRequest request) {
        //예약조회
        Reservation reservation = reservationRepository.findByIdWithTicket(request.getReservationId())
                .orElseThrow(ReservationNotFoundException::new);

        //소유자검증
        if (!reservation.isOwnedBy(userId)) {
            throw new ReservationAccessDeniedException("본인 예약만 결제가 가능합니다.");
        }

        //결제생성
        Payment payment = Payment.create(reservation, reservation.getTicket().getPrice(), request.getPaymentMethod());
        paymentRepository.save(payment);

        // 결제 완료처리
        payment.complete(LocalDateTime.now());

        // 예약 확정처리
        reservation.confirm();

        // 티켓 판매완료처리
        reservation.getTicket().updateStatus(TicketStatus.SOLD);

        // 티켓서버 예약 확정 호출
        ticketServerClient.confirmReservation(reservation.getExternalReservationId(), request.getPaymentMethod());

        return PaymentCreateResponse.from(payment, reservation);
    }
}
