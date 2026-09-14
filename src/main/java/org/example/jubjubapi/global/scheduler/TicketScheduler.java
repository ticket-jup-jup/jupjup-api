package org.example.jubjubapi.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.ticket.service.TicketService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketScheduler {

    private final TicketService ticketService;

    @Scheduled(cron = "0 * * * * *") // 1분마다 진행
    public void pollTickets() {
        log.info("티켓 polling 시작");
        ticketService.pollTickets();
        log.info("티켓 polling 종료");
    }
}