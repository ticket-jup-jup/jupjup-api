package org.example.jubjubapi.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.payment.dto.request.PaymentCreateRequest;
import org.example.jubjubapi.payment.dto.response.PaymentCancelResponse;
import org.example.jubjubapi.payment.dto.response.PaymentCreateResponse;
import org.example.jubjubapi.payment.dto.response.PaymentGetResponse;
import org.example.jubjubapi.payment.entity.Payment;
import org.example.jubjubapi.payment.exception.PaymentAccessDeniedException;
import org.example.jubjubapi.payment.exception.PaymentNotFoundException;
import org.example.jubjubapi.payment.repository.PaymentRepository;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.exception.InvalidPageRequestException;
import org.example.jubjubapi.reservation.exception.ReservationAccessDeniedException;
import org.example.jubjubapi.reservation.exception.ReservationNotFoundException;
import org.example.jubjubapi.reservation.repository.ReservationRepository;
import org.example.jubjubapi.ticket.client.TicketServerClient;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final TicketServerClient ticketServerClient;

    // 한 번에 조회 가능한 최대 개수 (페이징)
    private static final int MAX_PAGE_SIZE = 100;

    // 결제요청
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

    // 결제단건조회
    @Transactional(readOnly = true)
    public PaymentGetResponse getOnePayment(Long userId, Long paymentId) {

        // 결제조회 (N+1 방지)
        Payment payment = paymentRepository.findByIdWithReservationAndTicket(paymentId)
                .orElseThrow(PaymentNotFoundException::new);

        // 소유자검증
        Reservation reservation = payment.getReservation();
        if(!reservation.isOwnedBy(userId)) {
            throw new PaymentAccessDeniedException();
        }

        return PaymentGetResponse.from(payment);
    }

    // 결제전체조회
    @Transactional(readOnly = true)
    public List<PaymentGetResponse> getAllPayment(Long userId, int page, int size) {
        Pageable pageable = toPageable(page, size);
        // 결제전체조회 (N+1방지)
        List<Payment> payments = paymentRepository.findAllByUserId(userId, pageable);

        return payments.stream()
                .map(PaymentGetResponse::from)
                .toList();
    }

    // 결제취소(환불)
    public PaymentCancelResponse cancel(Long userId, Long paymentId) {
        // 결제조회
        Payment payment = paymentRepository.findByIdWithReservationAndTicket(paymentId)
                .orElseThrow(PaymentNotFoundException::new);
        Reservation reservation = payment.getReservation();

        // 소유자검증
        if (!reservation.isOwnedBy(userId)) {
            throw new PaymentAccessDeniedException();
        }

        // 환불처리
        payment.refund();

        // 예약취소
        reservation.cancel();

        // 티켓복구
        reservation.getTicket().updateStatus(TicketStatus.AVAILABLE);

        // 티켓서버 취소 요청
        ticketServerClient.cancelReservation(reservation.getExternalReservationId());

        return PaymentCancelResponse.from(payment);
    }

    private Pageable toPageable(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPageRequestException();
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    }
}
