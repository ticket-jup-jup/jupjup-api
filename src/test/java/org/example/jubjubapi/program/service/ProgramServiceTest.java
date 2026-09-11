package org.example.jubjubapi.program.service;

import org.example.jubjubapi.program.dto.TicketServerProgram;
import org.example.jubjubapi.program.entity.Program;
import org.example.jubjubapi.program.entity.ProgramType;
import org.example.jubjubapi.program.repository.ProgramRepository;
import org.example.jubjubapi.ticketserver.client.TicketServerClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private TicketServerClient ticketServerClient;

    @InjectMocks
    private ProgramService programService;

    @Test
    void 새로운_프로그램이면_생성() {
        // given
        TicketServerProgram program = new TicketServerProgram(
                1L,
                "뮤지컬 위키드",
                ProgramType.MUSICAL,
                "뮤지컬 공연"
        );

        when(ticketServerClient.getPrograms()).thenReturn(List.of(program));
        when(programRepository.findByExternalProgramId(1L)).thenReturn(Optional.empty());
        when(programRepository.findAllByDeletedAtIsNullAndExternalProgramIdNotIn(anySet())).thenReturn(List.of());

        // when
        programService.getTicketServerProgram();

        // then
        verify(programRepository).save(any(Program.class));
    }

    @Test
    void 기존_프로그램이면_업데이트() {
        // given
        Program existingProgram = new Program(
                1L,
                "기존 공연",
                ProgramType.MUSICAL,
                "기존 설명"
        );

        TicketServerProgram program = new TicketServerProgram(
                1L,
                "수정된 공연",
                ProgramType.MUSICAL,
                "수정된 설명"
        );

        when(ticketServerClient.getPrograms()).thenReturn(List.of(program));
        when(programRepository.findByExternalProgramId(1L)).thenReturn(Optional.of(existingProgram));
        when(programRepository.findAllByDeletedAtIsNullAndExternalProgramIdNotIn(anySet())).thenReturn(List.of());

        // when
        programService.getTicketServerProgram();

        // then
        assertThat(existingProgram.getName()).isEqualTo("수정된 공연");
        assertThat(existingProgram.getDescription()).isEqualTo("수정된 설명");

        verify(programRepository, never()).save(any(Program.class));
    }

    @Test
    void 티켓서버에_없는_프로그램은_삭제() {
        // given
        TicketServerProgram program = new TicketServerProgram(
                1L,
                "현재 공연",
                ProgramType.MUSICAL,
                "공연 설명"
        );

        Program deletedProgram = new Program(
                2L,
                "삭제된 공연",
                ProgramType.MUSICAL,
                "삭제된 공연 설명"
        );

        when(ticketServerClient.getPrograms()).thenReturn(List.of(program));
        when(programRepository.findByExternalProgramId(1L))
                .thenReturn(Optional.of(new Program(
                        1L,
                        "현재 공연",
                        ProgramType.MUSICAL,
                        "공연 설명"
                )));

        when(programRepository.findAllByDeletedAtIsNullAndExternalProgramIdNotIn(anySet())).thenReturn(List.of(deletedProgram));

        // when
        programService.getTicketServerProgram();

        // then
        assertThat(deletedProgram.getDeletedAt()).isNotNull();
    }
}