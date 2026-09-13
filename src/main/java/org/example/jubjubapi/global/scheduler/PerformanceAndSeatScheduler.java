package org.example.jubjubapi.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.performance.service.PerformanceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceAndSeatScheduler {

    private final PerformanceService performanceService;

    @Scheduled(cron = "0 0 * * * *")
    public void getTicketServerPerformanceAndSeat() {
        log.info("티켓서버 회차 및 좌석 목록 조회 시작");
        performanceService.getTicketServerPerformanceAndSeat();
        log.info("티켓서버 회차 및 좌석 목록 조회 종료");
    }
}
