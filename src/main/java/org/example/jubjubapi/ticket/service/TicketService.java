package org.example.jubjubapi.ticket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.notification.event.TicketCanceledEvent;
import org.example.jubjubapi.performance.service.PerformanceService;
import org.example.jubjubapi.performancewatch.service.PerformanceWatchService;
import org.example.jubjubapi.seat.entity.Seat;
import org.example.jubjubapi.seat.exception.SeatNotFoundException;
import org.example.jubjubapi.seat.repository.SeatRepository;
import org.example.jubjubapi.ticket.dto.TicketCanceledWebhookRequest;
import org.example.jubjubapi.ticket.dto.TicketResponse;
import org.example.jubjubapi.ticket.dto.TicketServerTicket;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.exception.TicketErrorCode;
import org.example.jubjubapi.ticket.exception.TicketException;
import org.example.jubjubapi.ticket.repository.TicketRepository;
import org.example.jubjubapi.ticketserver.client.TicketServerClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketServerClient ticketServerClient;
    private final PerformanceService performanceService;
    private final PerformanceWatchService performanceWatchService;
    private final SeatRepository seatRepository;
    private final ApplicationEventPublisher eventPublisher;

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
                .orElseThrow(() -> new TicketException(TicketErrorCode.TICKET_NOT_FOUND));
        return TicketResponse.from(ticket);
    }


    //티켓 데이터 삭제
    @Transactional
    public void deleteTicket(Long ticketId) {
        requirePositiveId(ticketId);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new TicketException(TicketErrorCode.TICKET_NOT_FOUND));
        if (ticketRepository.countReservationReferences(ticketId) > 0) {
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

    // 티켓서버의 티켓 정보를 polling
    @Transactional
    public void pollTickets() {
        // performanceWatchService를 호출해서 performance_id 목록 조회
        List<Long> performanceIds = performanceWatchService.getActivePerformanceIds();

        // performance_id 중복 제거
        Set<Long> uniquePerformanceIds = new HashSet<>(performanceIds);

        // performance_id로 performanceService 호출해서 external_performance_id 목록 조회
        for (Long performanceId : uniquePerformanceIds) {
            Long externalPerformanceId = performanceService.getExternalPerformanceId(performanceId);

            // external_performance_id로 티켓서버 티켓 조회 API 호출
            List<TicketServerTicket> tickets = ticketServerClient.getTickets(externalPerformanceId);

            // external_ticket_id로 ticket 데이터 조회
            for (TicketServerTicket ticket : tickets) {
                ticketRepository.findByExternalTicketId(ticket.getId())
                        // 기존 데이터가 있으면 업데이트
                        .ifPresentOrElse(
                                existingTicket -> {
                                    TicketStatus previousStatus = existingTicket.getStatus();

                                    existingTicket.update(
                                            ticket.getStatus(),
                                            ticket.getPrice()
                                    );

                                    // 기존 상태 SOLD + 조회된 상태 AVAILABLE => 취소표 알림 이벤트 발생
                                    if (previousStatus == TicketStatus.SOLD
                                            && ticket.getStatus() == TicketStatus.AVAILABLE) {
                                        publishTicketCanceledEvent(
                                                existingTicket,
                                                performanceId,
                                                LocalDateTime.now()
                                        );
                                    }
                                },

                                // 없으면 신규 티켓 데이터 생성
                                () -> {
                                    Seat seat = seatRepository.findByPerformanceIdAndExternalSeatId(
                                            performanceId,
                                            ticket.getSeatId()
                                    ).orElseThrow(() -> new SeatNotFoundException("존재하지 않는 좌석입니다."));

                                    ticketRepository.save(
                                            new Ticket(
                                                    ticket.getId(),
                                                    performanceId,
                                                    seat,
                                                    ticket.getPrice(),
                                                    ticket.getStatus()
                                            )
                                    );
                                }
                        );
            }
        }
    }

    @Transactional
    public void handleTicketCanceled(TicketCanceledWebhookRequest request) {
        // 취소표 알림 설정이 된 회차인지 확인
        // 취소표 알림 설정된 회차만 이후 로직 실행
        if (!performanceWatchService.hasActiveWatch(request.getPerformanceId())) {
            log.info("취소표 알림 설정이 없는 회차입니다. performanceId={}", request.getPerformanceId());
            return;
        }

        // external_ticket_id로 ticket 데이터 조회
        Ticket ticket = ticketRepository.findByExternalTicketId(request.getTicketId())
                // 기존 데이터가 있으면 업데이트
                .map(existingTicket -> {
                    TicketStatus previousStatus = existingTicket.getStatus();

                    existingTicket.update(
                            TicketStatus.AVAILABLE,
                            request.getPrice()
                    );

                    // 기존에 저장된 티켓 상태가 SOLD였으면 취소표 알림 이벤트 실행
                    if (previousStatus == TicketStatus.SOLD) {
                        publishTicketCanceledEvent(
                                existingTicket,
                                request.getPerformanceId(),
                                request.getCanceledAt()
                        );
                    }

                    return existingTicket;
                })
                // 없으면 신규 티켓 데이터 생성
                .orElseGet(() -> {
                    Seat seat = seatRepository
                            .findByPerformanceIdAndExternalSeatId(
                                    request.getPerformanceId(),
                                    request.getSeatId()
                            ).orElseThrow(() -> new SeatNotFoundException("존재하지 않는 좌석입니다."));

                    Ticket newTicket = ticketRepository.save(
                            new Ticket(
                                    request.getTicketId(),
                                    request.getPerformanceId(),
                                    seat,
                                    request.getPrice(),
                                    TicketStatus.AVAILABLE
                            )
                    );

                    // 취소표 알림 이벤트 실행
                    publishTicketCanceledEvent(
                            newTicket,
                            request.getPerformanceId(),
                            request.getCanceledAt()
                    );

                    return newTicket;
                });

        log.info("취소표 webhook 처리 완료. externalTicketId={}, performanceId={}",
                request.getTicketId(),
                request.getPerformanceId()
        );
    }

    //공통함수
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

    // 취소표 알림 이벤트 발생
    private void publishTicketCanceledEvent(Ticket ticket, Long performanceId, LocalDateTime canceledAt) {
        eventPublisher.publishEvent(
                new TicketCanceledEvent(
                        UUID.randomUUID().toString(),
                        ticket.getId(),
                        performanceId,
                        canceledAt
                )
        );
    }
}
