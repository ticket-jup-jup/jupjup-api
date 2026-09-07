package org.example.jubjubapi.ticket.polling;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketPollingScheduler {

    private final TicketPollingService ticketPollingService;

    @Scheduled(
            fixedDelayString =
                    "${ticket.polling.fixed-delay-ms:10000}"//10초마다 polling
    )
    public void pollTickets() {

        ticketPollingService.pollWatchedPerformances();
    }
}