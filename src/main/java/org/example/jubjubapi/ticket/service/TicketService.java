package org.example.jubjubapi.ticket.service;

import lombok.RequiredArgsConstructor;
//import org.example.jubjubapi.notification.repository.NotificationRepository;
import org.example.jubjubapi.ticket.dto.TicketResponse;
import org.example.jubjubapi.ticket.dto.TicketWatchResponse;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.entity.TicketWatch;
import org.example.jubjubapi.ticket.entity.TicketWatchStatus;
import org.example.jubjubapi.ticket.exception.TicketErrorCode;
import org.example.jubjubapi.ticket.exception.TicketException;
import org.example.jubjubapi.ticket.repository.TicketRepository;
import org.example.jubjubapi.ticket.repository.TicketWatchRepository;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                .orElseThrow(()->new TicketException(TicketErrorCode.TICKET_NOT_FOUND));
        return TicketResponse.from(ticket);
    }

    //취소표 알림 구독 생성
    @Transactional
    public TicketWatchResponse createWatch(Long userId, Long ticketId) {
        User user = findActiveUser(userId);
        requirePositiveId(ticketId);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(()->new TicketException(TicketErrorCode.TICKET_NOT_FOUND));

        TicketWatch watch = ticketWatchRepository.findByUser_IdAndTicket_Id(userId, ticketId)
                .orElse(null);
        if (watch != null && watch.isActive()) {
            throw new TicketException(TicketErrorCode.WATCH_ALREADY_EXISTS);
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
            throw new TicketException(TicketErrorCode.WATCH_CONFLICT);
        }
    }

    //취소표 알림 구독 목록 조회
    public List<TicketWatchResponse> getMyWatches(Long userId, TicketWatchStatus status,
                                                  int page, int size) {
        findActiveUser(userId);
        if (status == null) {
            throw new TicketException(TicketErrorCode.INVALID_WATCH_STATUS);
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
                .orElseThrow(()->new TicketException(TicketErrorCode.WATCH_NOT_FOUND));
        // 반복 해제도 성공한다. DB 행과 과거 알림은 삭제하지 않는다.
        watch.deactivate();
    }
    //티켓 데이터 삭제
    @Transactional
    public void deleteTicket(Long ticketId) {
        requirePositiveId(ticketId);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(()->new TicketException(TicketErrorCode.TICKET_NOT_FOUND));
        if (ticketWatchRepository.existsByTicket_Id(ticketId)
                //|| notificationRepository.existsByTicket_Id(ticketId)
                || ticketRepository.countReservationReferences(ticketId) > 0) {
            throw new TicketException(TicketErrorCode.TICKET_IN_USE);
        }

        if (ticketRepository.countRestrictiveReservationForeignKeys() == 0) {
            throw new TicketException(TicketErrorCode.TICKET_DELETE_NOT_READY);
        }
        try {
            ticketRepository.delete(ticket);
            ticketRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new TicketException(TicketErrorCode.TICKET_IN_USE_FK);
        }
    }
    //공통함수

    //활성 사용자 조회
    private User findActiveUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new TicketException(TicketErrorCode.AUTHENTICATION_REQUIRED);
        }
        return userRepository.findById(userId).filter(User::isActive)
                .orElseThrow(()->new TicketException(TicketErrorCode.USER_UNAVAILABLE ));
    }
    //ID 검증
    private void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new TicketException(TicketErrorCode.INVALID_ID);
        }
    }
    //페이지 요청 객체 생성
    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw new TicketException(TicketErrorCode.INVALID_PAGINATION);
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    }


}
