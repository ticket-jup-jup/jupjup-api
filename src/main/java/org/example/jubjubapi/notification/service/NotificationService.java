package org.example.jubjubapi.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.notification.dto.NotificationResponse;
import org.example.jubjubapi.notification.entity.Notification;
import org.example.jubjubapi.notification.event.TicketCanceledEvent;
import org.example.jubjubapi.notification.exception.NotificationTicketNotFoundException;
import org.example.jubjubapi.notification.repository.NotificationRepository;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.repository.TicketRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TicketRepository ticketRepository;
    private final PerformanceWatchRepository performanceWatchRepository;   // 예슬님 작업 확인후 수정필요

    @Transactional
    public void notifySubscribers(TicketCanceledEvent event) {
        Ticket ticket = ticketRepository.findById(event.ticketId())
                .orElseThrow(() -> new NotificationTicketNotFoundException(event.ticketId()));

        List<PerformanceWatch> watches = performanceWatchRepository
                .findByPerformance_IdAndStatus(event.performanceId(), PerformanceWatchStatus.ACTIVE);

        int created = 0;
        for (PerformanceWatch watch : watches) {
            Long userId = watch.getUser().getId();

            // 1차: 코드 레벨 중복 체크 (웹훅/폴링 중복, Kafka 재전송)
            if (notificationRepository.existsByEventIdAndUser_Id(event.eventId(), userId)) {
                continue;
            }
            try {
                notificationRepository.save(Notification.ticketCanceled(
                        event.eventId(), watch.getUser(), ticket, event.performanceId(), event.canceledAt()));
                created++;
            } catch (DataIntegrityViolationException e) {
                // 2차: 동시 처리로 유니크 제약에 걸린 경우 — 이미 알림 있음, 정상 케이스
                log.debug("중복 알림 스킵. eventId={}, userId={}", event.eventId(), userId);
            }
            // 구독 유지 — 같은 회차에 다음 취소표가 나오면 다시 알림 (eventId가 다르므로 중복 아님)
        }
        log.info("취소표 알림 생성 완료. eventId={}, 구독자={}명, 생성={}건",
                event.eventId(), watches.size(), created);
    }

    /** GET /api/notifications */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long userId, int page, int size) {
        Page<Notification> result = notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return result.map(NotificationResponse::from).getContent();
    }
}
