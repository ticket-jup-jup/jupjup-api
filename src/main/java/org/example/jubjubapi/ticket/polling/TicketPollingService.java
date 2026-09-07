package org.example.jubjubapi.ticket.polling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.ticket.client.TicketServerClient;
import org.example.jubjubapi.ticket.client.dto.response.TicketServerTicketResponse;
import org.example.jubjubapi.ticket.detection.TicketDetectionSource;
import org.example.jubjubapi.ticket.detection.TicketStatusSyncService;
import org.example.jubjubapi.ticket.entity.TicketWatchStatus;
import org.example.jubjubapi.ticket.repository.TicketWatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketPollingService {

    private final TicketWatchRepository ticketWatchRepository;
    private final TicketServerClient ticketServerClient;
    private final TicketStatusSyncService ticketStatusSyncService;

    public void pollWatchedPerformances() {

        List<Long> performanceIds =
                ticketWatchRepository.findDistinctPerformanceIdsByStatus(
                        TicketWatchStatus.ACTIVE
                );

        if (performanceIds.isEmpty()) {
            return;
        }

        for (Long performanceId : performanceIds) {

            try {
                List<TicketServerTicketResponse> externalTickets =
                        ticketServerClient.getInternalTickets(performanceId);

                for (TicketServerTicketResponse externalTicket : externalTickets) {

                    ticketStatusSyncService.syncStatus(
                            externalTicket.getId(),
                            externalTicket.getStatus(),
                            TicketDetectionSource.POLLING
                    );
                }

            } catch (Exception e) {
                // 한 회차 조회 실패 때문에 전체 Polling이 멈추지 않게 한다.
                log.warn(
                        "Polling 처리 실패: performanceId={}, message={}",
                        performanceId,
                        e.getMessage()
                );
            }
        }
    }
}