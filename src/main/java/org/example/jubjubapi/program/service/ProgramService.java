package org.example.jubjubapi.program.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.program.dto.TicketServerProgram;
import org.example.jubjubapi.program.entity.Program;
import org.example.jubjubapi.program.repository.ProgramRepository;
import org.example.jubjubapi.ticketserver.client.TicketServerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final TicketServerClient ticketServerClient;

    @Transactional
    public void getTicketServerProgram() {
        // 티켓서버 호출
        List<TicketServerProgram> programs = ticketServerClient.getPrograms();

        // 티켓서버에 존재하는 프로그램 ID 목록 Set 생성
        Set<Long> externalProgramIds = programs.stream()
                .map(TicketServerProgram::getId)
                .collect(Collectors.toSet());

        for (TicketServerProgram program : programs) {
            // 기존 데이터 있는지 확인
            programRepository.findByExternalProgramId(program.getId())
                    .ifPresentOrElse(
                            // 있으면 업데이트
                            existingProgram -> existingProgram.update(
                                    program.getName(),
                                    program.getType(),
                                    program.getDescription()
                            ),
                            // 없으면 생성
                            () -> programRepository.save(
                                    new Program(
                                            program.getId(),
                                            program.getName(),
                                            program.getType(),
                                            program.getDescription()
                                    )
                            )
                    );
        }

        // 티켓서버에서 삭제된 프로그램 삭제
        List<Program> deletedPrograms = programRepository.findAllByDeletedAtIsNullAndExternalProgramIdNotIn((externalProgramIds));
        deletedPrograms.forEach(Program::delete);
    }
}
