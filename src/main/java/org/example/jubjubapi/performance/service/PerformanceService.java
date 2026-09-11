package org.example.jubjubapi.performance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.performance.dto.TicketServerPerformance;
import org.example.jubjubapi.performance.entity.Performance;
import org.example.jubjubapi.performance.repository.PerformanceRepository;
import org.example.jubjubapi.program.entity.Program;
import org.example.jubjubapi.program.repository.ProgramRepository;
import org.example.jubjubapi.ticketserver.client.TicketServerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final ProgramRepository programRepository;
    private final TicketServerClient ticketServerClient;

    @Transactional
    public void getTicketServerPerformance() {
        // 프로그램 목록 조회
        List<Program> programs = programRepository.findAllByDeletedAtIsNull();

        // 프로그램별 회차 정보 조회
        for (Program program : programs) {
            log.info("program id : {}", program.getId()); // 1 2 3

            // 티켓서버 호출
            List<TicketServerPerformance> performances = ticketServerClient.getPerformances(program.getExternalProgramId());

            // 티켓서버에 존재하는 회차 ID 목록 Set 생성
            Set<Long> externalPerformanceIds = performances.stream()
                    .map(TicketServerPerformance::getId)
                    .collect(Collectors.toSet());

            for (TicketServerPerformance performance : performances) {
                // 기존 데이터 있는지 확인
                performanceRepository.findByExternalPerformanceId(performance.getId())
                        .ifPresentOrElse(
                                // 있으면 업데이트
                                existingPerformance -> existingPerformance.update(
                                        performance.getStartAt(),
                                        performance.getEndAt(),
                                        performance.getVenue(),
                                        performance.getStatus()
                                ),
                                // 없으면 생성
                                () -> performanceRepository.save(
                                        new Performance(
                                                program,
                                                performance.getId(),
                                                performance.getStartAt(),
                                                performance.getEndAt(),
                                                performance.getVenue(),
                                                performance.getStatus()
                                        )
                                )
                        );
            }

            // 티켓서버에서 삭제된 회차 삭제
            List<Performance> deletedPerformances =
                    performanceRepository.findAllByProgramIdAndDeletedAtIsNullAndExternalPerformanceIdNotIn(program.getId(), externalPerformanceIds);

            deletedPerformances.forEach(performance ->
                    log.info(
                            "삭제 대상 회차: jupJupId={}, externalPerformanceId={}, programId={}",
                            performance.getId(),
                            performance.getExternalPerformanceId(),
                            performance.getProgram().getId()
                    )
            );

            deletedPerformances.forEach(Performance::delete);
        }
    }
}
