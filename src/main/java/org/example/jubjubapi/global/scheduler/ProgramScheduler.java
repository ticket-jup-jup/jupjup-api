package org.example.jubjubapi.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.program.service.ProgramService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProgramScheduler {

    private final ProgramService programService;

    @Scheduled(cron = "0 0 * * * *") // 매시간 정각마다 실행
    public void getTicketServerProgram() {
        log.info("티켓서버 프로그램 목록 조회 시작");
        programService.getTicketServerProgram();
        log.info("티켓서버 프로그램 목록 조회 종료");
    }
}
