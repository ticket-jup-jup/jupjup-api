package org.example.jubjubapi.scheduler.controller;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.performance.service.PerformanceService;
import org.example.jubjubapi.program.service.ProgramService;
import org.example.jubjubapi.reservation.service.ReservationService;
import org.example.jubjubapi.ticket.service.TicketService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final ProgramService programService;
    private final PerformanceService performanceService;
    private final TicketService ticketService;
    private final ReservationService reservationService;

    // 프로그램 동기화
    @PostMapping("/program-sync")
    public void programSync() {
        programService.getTicketServerProgram();
    }

    // 회차 및 좌석 동기화
    @PostMapping("/performance-seat-sync")
    public void performanceAndSeatSync() {
        performanceService.getTicketServerPerformanceAndSeat();
    }

    // 티켓 동기화
    @PostMapping("/ticket-polling")
    public void ticketPolling() {
        ticketService.pollTickets();
    }

    // 임시예약 결제 시간 만료
    @PostMapping("/reservations-expire")
    public void expireReservations() {
        reservationService.expireReservations();
    }
}