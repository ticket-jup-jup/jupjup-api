package org.example.jubjubapi.performance.service;

import org.example.jubjubapi.performance.dto.TicketServerPerformance;
import org.example.jubjubapi.performance.entity.Performance;
import org.example.jubjubapi.performance.entity.PerformanceStatus;
import org.example.jubjubapi.performance.repository.PerformanceRepository;
import org.example.jubjubapi.program.entity.Program;
import org.example.jubjubapi.program.entity.ProgramType;
import org.example.jubjubapi.program.repository.ProgramRepository;
import org.example.jubjubapi.seat.dto.TicketServerSeat;
import org.example.jubjubapi.seat.service.SeatService;
import org.example.jubjubapi.ticketserver.client.TicketServerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("회차 및 좌석 동기화 서비스 테스트")
class PerformanceServiceTest {

    @Mock
    private PerformanceRepository performanceRepository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private TicketServerClient ticketServerClient;

    @Mock
    private SeatService seatService;

    @InjectMocks
    private PerformanceService performanceService;

    private Program program;

    @BeforeEach
    void setUp() {
        program = new Program(
                100L,
                "테스트 공연",
                ProgramType.MUSICAL,
                "테스트 공연 설명"
        );

        ReflectionTestUtils.setField(program, "id", 1L);
    }

    @Test
    void 신규_회차_저장_및_좌석_동기화() {
        // given
        TicketServerPerformance ticketServerPerformance = new TicketServerPerformance(
                1L,
                100L,
                LocalDateTime.of(2026, 10, 1, 19, 0),
                LocalDateTime.of(2026, 10, 1, 21, 0),
                "공연장",
                PerformanceStatus.UPCOMING
        );

        TicketServerSeat seat = mock(TicketServerSeat.class);

        when(programRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(program));
        when(ticketServerClient.getPerformances(100L)).thenReturn(List.of(ticketServerPerformance));
        when(performanceRepository.findByExternalPerformanceId(1L)).thenReturn(Optional.empty());
        when(performanceRepository.findAllByProgramIdAndDeletedAtIsNullAndExternalPerformanceIdNotIn(
                eq(1L),
                eq(Set.of(1L))
        )).thenReturn(List.of());

        when(ticketServerClient.getSeats(1L)).thenReturn(List.of(seat));

        // when
        performanceService.getTicketServerPerformanceAndSeat();

        // then
        ArgumentCaptor<Performance> captor = ArgumentCaptor.forClass(Performance.class);

        verify(performanceRepository).save(captor.capture());
        Performance savedPerformance = captor.getValue();

        assertThat(savedPerformance.getProgram()).isSameAs(program);
        assertThat(savedPerformance.getExternalPerformanceId()).isEqualTo(1L);
        assertThat(savedPerformance.getStartAt()).isEqualTo(LocalDateTime.of(2026, 10, 1, 19, 0));
        assertThat(savedPerformance.getEndAt()).isEqualTo(LocalDateTime.of(2026, 10, 1, 21, 0));
        assertThat(savedPerformance.getVenue()).isEqualTo("공연장");
        assertThat(savedPerformance.getStatus()).isEqualTo(PerformanceStatus.UPCOMING);

        verify(ticketServerClient).getPerformances(100L);

        verify(ticketServerClient).getSeats(1L);
        verify(seatService).syncSeats(savedPerformance, List.of(seat));
    }

    @Test
    void 기존_회차_업데이트_및_좌석_동기화() {
        // given
        TicketServerPerformance ticketServerPerformance = new TicketServerPerformance(
                1L,
                100L,
                LocalDateTime.of(2026, 10, 1, 20, 0),
                LocalDateTime.of(2026, 10, 1, 22, 0),
                "새로운 공연장",
                PerformanceStatus.ONGOING
        );

        Performance existingPerformance = new Performance(
                program,
                1L,
                LocalDateTime.of(2026, 10, 1, 19, 0),
                LocalDateTime.of(2026, 10, 1, 21, 0),
                "기존 공연장",
                PerformanceStatus.UPCOMING
        );

        TicketServerSeat seat = mock(TicketServerSeat.class);

        when(programRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(program));
        when(ticketServerClient.getPerformances(100L)).thenReturn(List.of(ticketServerPerformance));
        when(performanceRepository.findByExternalPerformanceId(1L)).thenReturn(Optional.of(existingPerformance));
        when(performanceRepository.findAllByProgramIdAndDeletedAtIsNullAndExternalPerformanceIdNotIn(
                eq(1L),
                eq(Set.of(1L))
        )).thenReturn(List.of());

        when(ticketServerClient.getSeats(1L)).thenReturn(List.of(seat));

        // when
        performanceService.getTicketServerPerformanceAndSeat();

        // then
        assertThat(existingPerformance.getStartAt()).isEqualTo(LocalDateTime.of(2026, 10, 1, 20, 0));
        assertThat(existingPerformance.getEndAt()).isEqualTo(LocalDateTime.of(2026, 10, 1, 22, 0));
        assertThat(existingPerformance.getVenue()).isEqualTo("새로운 공연장");
        assertThat(existingPerformance.getStatus()).isEqualTo(PerformanceStatus.ONGOING);

        verify(performanceRepository, never()).save(any(Performance.class));

        verify(ticketServerClient).getPerformances(100L);
        verify(ticketServerClient).getSeats(1L);
        verify(seatService).syncSeats(existingPerformance, List.of(seat));
    }

    @Test
    void 삭제된_회차_삭제() {
        TicketServerPerformance ticketServerPerformance = new TicketServerPerformance(
                1L,
                100L,
                LocalDateTime.of(2026, 10, 1, 19, 0),
                LocalDateTime.of(2026, 10, 1, 21, 0),
                "공연장",
                PerformanceStatus.UPCOMING
        );

        Performance existingPerformance = new Performance(
                program,
                1L,
                LocalDateTime.of(2026, 10, 1, 19, 0),
                LocalDateTime.of(2026, 10, 1, 21, 0),
                "공연장",
                PerformanceStatus.UPCOMING
        );

        Performance deletedPerformance = new Performance(
                program,
                2L,
                LocalDateTime.of(2026, 10, 2, 19, 0),
                LocalDateTime.of(2026, 10, 2, 21, 0),
                "공연장",
                PerformanceStatus.UPCOMING
        );

        TicketServerSeat seat = mock(TicketServerSeat.class);

        when(programRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(program));
        when(ticketServerClient.getPerformances(100L)).thenReturn(List.of(ticketServerPerformance));

        when(performanceRepository.findByExternalPerformanceId(1L)).thenReturn(Optional.of(existingPerformance));
        when(ticketServerClient.getSeats(1L)).thenReturn(List.of(seat));

        when(performanceRepository.findAllByProgramIdAndDeletedAtIsNullAndExternalPerformanceIdNotIn(
                eq(1L),
                eq(Set.of(1L))
        )).thenReturn(List.of(deletedPerformance));

        performanceService.getTicketServerPerformanceAndSeat();
        assertThat(deletedPerformance.getDeletedAt()).isNotNull();

        verify(performanceRepository)
                .findAllByProgramIdAndDeletedAtIsNullAndExternalPerformanceIdNotIn(
                        eq(1L),
                        eq(Set.of(1L))
                );

        verify(ticketServerClient).getSeats(1L);
        verify(seatService).syncSeats(existingPerformance, List.of(seat));
    }

    @Test
    void 프로그램별_회차_및_좌석_동기화() {
        Program secondProgram = new Program(
                200L,
                "두 번째 공연",
                ProgramType.MUSICAL,
                "두 번째 공연 설명"
        );

        ReflectionTestUtils.setField(secondProgram, "id", 2L);

        TicketServerPerformance firstPerformance = new TicketServerPerformance(
                1L,
                100L,
                LocalDateTime.of(2026, 10, 1, 19, 0),
                LocalDateTime.of(2026, 10, 1, 21, 0),
                "공연장 1",
                PerformanceStatus.UPCOMING
        );

        TicketServerPerformance secondPerformance = new TicketServerPerformance(
                2L,
                200L,
                LocalDateTime.of(2026, 10, 2, 19, 0),
                LocalDateTime.of(2026, 10, 2, 21, 0),
                "공연장 2",
                PerformanceStatus.UPCOMING
        );

        TicketServerSeat firstSeat = mock(TicketServerSeat.class);
        TicketServerSeat secondSeat = mock(TicketServerSeat.class);

        Performance firstSavedPerformance = new Performance(
                program,
                1L,
                LocalDateTime.of(2026, 10, 1, 19, 0),
                LocalDateTime.of(2026, 10, 1, 21, 0),
                "공연장 1",
                PerformanceStatus.UPCOMING
        );

        Performance secondSavedPerformance = new Performance(
                secondProgram,
                2L,
                LocalDateTime.of(2026, 10, 2, 19, 0),
                LocalDateTime.of(2026, 10, 2, 21, 0),
                "공연장 2",
                PerformanceStatus.UPCOMING
        );

        when(programRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(program, secondProgram));

        when(ticketServerClient.getPerformances(100L)).thenReturn(List.of(firstPerformance));
        when(ticketServerClient.getPerformances(200L)).thenReturn(List.of(secondPerformance));

        when(performanceRepository.findByExternalPerformanceId(1L)).thenReturn(Optional.of(firstSavedPerformance));
        when(performanceRepository.findByExternalPerformanceId(2L)).thenReturn(Optional.of(secondSavedPerformance));

        when(ticketServerClient.getSeats(1L)).thenReturn(List.of(firstSeat));
        when(ticketServerClient.getSeats(2L)).thenReturn(List.of(secondSeat));

        when(performanceRepository.findAllByProgramIdAndDeletedAtIsNullAndExternalPerformanceIdNotIn(
                eq(1L),
                eq(Set.of(1L))
        )).thenReturn(List.of());
        when(performanceRepository.findAllByProgramIdAndDeletedAtIsNullAndExternalPerformanceIdNotIn(
                eq(2L),
                eq(Set.of(2L))
        )).thenReturn(List.of());

        performanceService.getTicketServerPerformanceAndSeat();

        verify(ticketServerClient).getPerformances(100L);
        verify(ticketServerClient).getPerformances(200L);

        verify(ticketServerClient).getSeats(1L);
        verify(ticketServerClient).getSeats(2L);

        verify(seatService).syncSeats(firstSavedPerformance, List.of(firstSeat));
        verify(seatService).syncSeats(secondSavedPerformance, List.of(secondSeat));

        verify(performanceRepository)
                .findAllByProgramIdAndDeletedAtIsNullAndExternalPerformanceIdNotIn(
                        eq(1L),
                        eq(Set.of(1L))
                );
        verify(performanceRepository)
                .findAllByProgramIdAndDeletedAtIsNullAndExternalPerformanceIdNotIn(
                        eq(2L),
                        eq(Set.of(2L))
                );
    }
}