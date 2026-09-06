package org.example.jubjubapi.ticket.detection;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketStatusSyncService {
    private final TicketRepository ticketRepository;
    @Transactional
    public void syncStatus(
            Long externalTicketId,
            TicketStatus newStatus,
            TicketDetectionSource source
    ) {
        Ticket ticket = ticketRepository
                .findByExternalTicketIdForUpdate(externalTicketId)
                .orElse(null);

        if (ticket == null) {
            log.warn(
                    "[{}] 로컬에 없는 외부 티켓입니다. externalTicketId={}",
                    source,
                    externalTicketId
            );
            return;
        }

        TicketStatus oldStatus = ticket.getStatus();

        // Webhook과 Polling이 같은 상태를 가져와도 중복 처리하지 않는다.
        if (oldStatus == newStatus) {
            return;
        }

        ticket.updateStatus(newStatus);

        log.info(
                "[티켓 상태 변경][{}] externalTicketId={}, {} -> {}",
                source,
                externalTicketId,
                oldStatus,
                newStatus
        );

        // ★ 나중에 Kafka 알림 이벤트를 발생시킬 위치
        if (newStatus == TicketStatus.AVAILABLE
                && oldStatus != TicketStatus.AVAILABLE) {

            log.info(
                    "========== 취소표 감지 [{}] externalTicketId={} ==========",
                    source,
                    externalTicketId
            );
        }
    }

}
