package org.example.jubjubapi.global.internal.controller;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.performance.service.PerformanceService;
import org.example.jubjubapi.program.service.ProgramService;
import org.example.jubjubapi.ticket.service.TicketService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal")
public class InternalSyncController {

    private final ProgramService programService;
    private final PerformanceService performanceService;
    private final TicketService ticketService;

    // 티켓서버 프로그램 수동 동기화
    @PostMapping("/programs/sync")
    public void syncPrograms() {
        programService.getTicketServerProgram();
    }

    // 티켓서버 회차 수동 동기화(좌석 정보 포함)
    @PostMapping("/performances/sync")
    public void syncPerformances() {
        performanceService.getTicketServerPerformanceAndSeat();
    }

    // 티켓서버 티켓 수동 동기화
    @PostMapping("/tickets/sync")
    public void syncTickets() {
        ticketService.pollTickets();
    }
}
