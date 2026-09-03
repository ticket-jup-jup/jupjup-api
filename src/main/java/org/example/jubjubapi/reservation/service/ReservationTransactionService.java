package org.example.jubjubapi.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.reservation.dto.request.ReservationCreateRequest;
import org.example.jubjubapi.reservation.dto.response.ReservationCreateResponse;
import org.example.jubjubapi.reservation.dto.response.ReservationGetResponse;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.exception.InvalidPageRequestException;
import org.example.jubjubapi.reservation.repository.ReservationRepository;
import org.example.jubjubapi.ticket.client.TicketServerClient;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.exception.TicketErrorCode;
import org.example.jubjubapi.ticket.exception.TicketException;
import org.example.jubjubapi.ticket.repository.TicketRepository;
import org.example.jubjubapi.ticketserver.entity.TicketServerAccount;
import org.example.jubjubapi.ticketserver.exception.TicketServerAccountNotLinkedException;
import org.example.jubjubapi.ticketserver.repository.TicketServerAccountRepository;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.exception.UserNotFoundException;
import org.example.jubjubapi.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 트랜잭션 담당 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationTransactionService {

    // 임시 예약 유효 시간
    private static final int RESERVATION_EXPIRE_MINUTES = 10;
    // 한 번에 조회 가능한 최대 개수 (페이징)
    private static final int MAX_PAGE_SIZE = 100;

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketServerClient ticketServerClient;
    private final TicketServerAccountRepository ticketServerAccountRepository;

    // 취소표 임시 예약 생성
    public ReservationCreateResponse reserve(Long userId, ReservationCreateRequest request) {

        // 유저조회
        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(UserNotFoundException::new);

        // 티켓조회
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new TicketException(TicketErrorCode.TICKET_NOT_FOUND));

        // 예약 가능 여부 확인
        if (ticket.getStatus() != TicketStatus.AVAILABLE) {
            throw new TicketException(TicketErrorCode.TICKET_NOT_AVAILABLE);
        }

        // 티켓서버 계정 연동 확인
        TicketServerAccount account = ticketServerAccountRepository.findByUserId(userId)
                .orElseThrow(TicketServerAccountNotLinkedException::new);

        // 임시 예약 생성
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = Reservation.create(user, ticket, now.plusMinutes(RESERVATION_EXPIRE_MINUTES));

        reservationRepository.save(reservation);

        // 티켓 상태 변경(예약)
        ticket.updateStatus(TicketStatus.RESERVED);

        // 티켓 서버에 임시 예약 요청
        Long externalReservationId = ticketServerClient.createTemporaryReservation(account.getExternalUserId(), ticket.getExternalTicketId());

        // 외부 예약 id 연결
        reservation.linkExternalReservation(externalReservationId);

        return ReservationCreateResponse.from(reservation);
    }

    // 나의 예약 전체 조회
    @Transactional(readOnly = true)
    public List<ReservationGetResponse> getMyReservation(Long userId, int page, int size) {
        Pageable pageable = toPageable(page, size);
        List<Reservation> reservations = reservationRepository.findAllByUserId(userId, pageable);

        return reservations.stream()
                .map(ReservationGetResponse::from)
                .toList();
    }

    private Pageable toPageable(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPageRequestException();
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    }
}
