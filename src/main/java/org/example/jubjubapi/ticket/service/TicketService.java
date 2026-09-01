package org.example.jubjubapi.ticket.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.global.exception.ServiceException;
//import org.example.jubjubapi.notification.repository.NotificationRepository;
import org.example.jubjubapi.ticket.dto.TicketResponse;
import org.example.jubjubapi.ticket.dto.TicketWatchResponse;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.entity.TicketWatch;
import org.example.jubjubapi.ticket.entity.TicketWatchStatus;
import org.example.jubjubapi.ticket.repository.TicketRepository;
import org.example.jubjubapi.ticket.repository.TicketWatchRepository;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketWatchRepository ticketWatchRepository;
    //private final NotificationRepository notificationRepository;(이후 추가 예정)
    private final UserRepository userRepository;

    //티켓목록조회
    public List<TicketResponse> getTickets(Long performanceId, TicketStatus status,
                                           int page, int size) {
        if (performanceId != null) {
            requirePositiveId(performanceId);
        }
        return ticketRepository.search(performanceId, status, pageable(page, size))
                .stream().map(TicketResponse::from).toList();
    }
    //티켓 상세 조회
    public TicketResponse getTicket(Long ticketId) {
        requirePositiveId(ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> notFound("TICKET_NOT_FOUND", "티켓을 찾을 수 없습니다."));
        return TicketResponse.from(ticket);
    }

    //취소표 알림 구독 생성
    @Transactional
    public TicketWatchResponse createWatch(Long userId, Long ticketId) {
        User user = findActiveUser(userId);
        requirePositiveId(ticketId);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> notFound("TICKET_NOT_FOUND", "티켓을 찾을 수 없습니다."));

        TicketWatch watch = ticketWatchRepository.findByUser_IdAndTicket_Id(userId, ticketId)
                .orElse(null);
        if (watch != null && watch.isActive()) {
            throw conflict("WATCH_ALREADY_EXISTS", "이미 구독 중인 티켓입니다.");
        }
        if (watch == null) {
            watch = TicketWatch.create(user, ticket);
        } else {
            // 기존 알림이 참조하는 watchId를 유지한다.
            watch.activate();
        }

        try {
            return TicketWatchResponse.from(ticketWatchRepository.saveAndFlush(watch));
        } catch (DataIntegrityViolationException ex) {
            throw conflict("WATCH_CONFLICT", "구독 저장 중 데이터 충돌이 발생했습니다.");
        }
    }

    //취소표 알림 구독 목록 조회
    public List<TicketWatchResponse> getMyWatches(Long userId, TicketWatchStatus status,
                                                  int page, int size) {
        findActiveUser(userId);
        if (status == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "INVALID_WATCH_STATUS",
                    "구독 상태는 필수입니다.");
        }
        return ticketWatchRepository.findByUser_IdAndStatus(userId, status, pageable(page, size))
                .stream().map(TicketWatchResponse::from).toList();
    }
    //취소표 알림 구독 해제
    @Transactional
    public void deactivateWatch(Long userId, Long watchId) {
        findActiveUser(userId);
        requirePositiveId(watchId);
        TicketWatch watch = ticketWatchRepository.findByIdAndUser_Id(watchId, userId)
                .orElseThrow(() -> notFound("WATCH_NOT_FOUND", "내 구독을 찾을 수 없습니다."));
        // 반복 해제도 성공한다. DB 행과 과거 알림은 삭제하지 않는다.
        watch.deactivate();
    }
    //티켓 데이터 삭제
    @Transactional
    public void deleteTicket(Long ticketId) {
        requirePositiveId(ticketId);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> notFound("TICKET_NOT_FOUND", "티켓을 찾을 수 없습니다."));
        if (ticketWatchRepository.existsByTicket_Id(ticketId)
                //|| notificationRepository.existsByTicket_Id(ticketId)
                || ticketRepository.countReservationReferences(ticketId) > 0) {
            throw conflict("TICKET_IN_USE", "구독·알림·예약이 연결된 티켓은 삭제할 수 없습니다.");
        }

        if (ticketRepository.countRestrictiveReservationForeignKeys() == 0) {
            throw conflict("TICKET_DELETE_NOT_READY",
                    "예약 참조를 보호하는 DB 외래키 설정 후 티켓을 삭제할 수 있습니다.");
        }
        try {
            ticketRepository.delete(ticket);
            ticketRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw conflict("TICKET_IN_USE", "다른 데이터에서 참조 중인 티켓은 삭제할 수 없습니다.");
        }
    }
    //공통함수

    //활성 사용자 조회
    private User findActiveUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ServiceException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED",
                    "로그인이 필요합니다.");
        }
        return userRepository.findById(userId).filter(User::isActive)
                .orElseThrow(() -> new ServiceException(HttpStatus.UNAUTHORIZED,
                        "USER_UNAVAILABLE", "사용 가능한 회원이 아닙니다."));
    }
    //ID 검증
    private void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "INVALID_ID",
                    "ID는 1 이상이어야 합니다.");
        }
    }
    //페이지 요청 객체 생성
    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "INVALID_PAGINATION",
                    "page는 0 이상, size는 1~100이어야 하며 조회 범위가 너무 크면 안 됩니다.");
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    }
    //404 NOT_FOUND 예외 생성
    private ServiceException notFound(String code, String message) {
        return new ServiceException(HttpStatus.NOT_FOUND, code, message);
    }
    //409 CONFLICT 예외 생성
    private ServiceException conflict(String code, String message) {
        return new ServiceException(HttpStatus.CONFLICT, code, message);
    }
}
