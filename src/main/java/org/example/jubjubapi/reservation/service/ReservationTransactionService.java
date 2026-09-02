package org.example.jubjubapi.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.reservation.dto.request.ReservationCreateRequest;
import org.example.jubjubapi.reservation.dto.response.ReservationCreateResponse;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.repository.ReservationRepository;
import org.example.jubjubapi.ticket.client.TicketServerClient;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.exception.TicketNotAvailableException;
import org.example.jubjubapi.ticket.exception.TicketNotFoundException;
import org.example.jubjubapi.ticket.repository.TicketRepository;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.exception.UserNotFoundException;
import org.example.jubjubapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 트랜잭션 담당 클래스
 */
@Service
@RequiredArgsConstructor
public class ReservationTransactionService {

    // 임시 예약 유효 시간
    private static final int RESERVATION_EXPIRE_MINUTES = 10;

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketServerClient ticketServerClient;

    // 취소표 임시 예약 생성 -> 분산락은 별도 이슈에서 적용 예정
    @Transactional
    public ReservationCreateResponse reserve(Long userId, ReservationCreateRequest request) {

        // 유저조회
        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(UserNotFoundException::new);

        // 티켓조회
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(TicketNotFoundException::new);

        // 예약 가능 여부 확인
        if (ticket.getStatus() != TicketStatus.AVAILABLE) {
            throw new TicketNotAvailableException();
        }

        // 임시 예약 생성
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = Reservation.create(user, ticket, now.plusMinutes(RESERVATION_EXPIRE_MINUTES));

        reservationRepository.save(reservation);

        // 티켓 상태 변경(예약)
        ticket.updateStatus(TicketStatus.RESERVED);

        // 티켓 서버에 임시 예약 요청 -> externalUserId는 TicketServerAccount 연동되면 교체 예정
        Long externalReservationId = ticketServerClient.createTemporaryReservation(userId, ticket.getExternalTicketId());

        // 외부 예약 id 연결
        reservation.linkExternalReservation(externalReservationId);

        return ReservationCreateResponse.from(reservation);
    }
}
