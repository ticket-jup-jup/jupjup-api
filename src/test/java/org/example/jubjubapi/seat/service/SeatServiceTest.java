package org.example.jubjubapi.seat.service;

import org.example.jubjubapi.performance.entity.Performance;
import org.example.jubjubapi.seat.dto.TicketServerSeat;
import org.example.jubjubapi.seat.entity.Seat;
import org.example.jubjubapi.seat.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketServerSeat ticketServerSeat;

    @Mock
    private Performance performance;

    @Mock
    private Seat existingSeat;

    @InjectMocks
    private SeatService seatService;

    @BeforeEach
    void setUp() {
        when(performance.getId()).thenReturn(1L);

        when(ticketServerSeat.getId()).thenReturn(100L);
        when(ticketServerSeat.getSection()).thenReturn("A");
        when(ticketServerSeat.getSeatRow()).thenReturn("1");
        when(ticketServerSeat.getSeatNumber()).thenReturn(1);
    }

    @Test
    void 신규_좌석_저장() {
        // given
        when(seatRepository.findByPerformanceIdAndExternalSeatId(1L, 100L))
                .thenReturn(Optional.empty());

        // when
        seatService.syncSeats(performance, List.of(ticketServerSeat));

        // then
        verify(seatRepository).findByPerformanceIdAndExternalSeatId(1L, 100L);
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void 기존_좌석_정보_변경() {
        // given
        when(seatRepository.findByPerformanceIdAndExternalSeatId(1L, 100L))
                .thenReturn(Optional.of(existingSeat));

        when(existingSeat.isSameInfo(performance, 100L, "A", "1", 1))
                .thenReturn(false);

        // when
        seatService.syncSeats(performance, List.of(ticketServerSeat));

        // then
        verify(seatRepository).findByPerformanceIdAndExternalSeatId(1L, 100L);
        verify(existingSeat).update("A", "1", 1);
        verify(seatRepository, never()).save(any(Seat.class));
    }

    @Test
    void 기존_좌석_정보_동일() {
        // given
        when(seatRepository.findByPerformanceIdAndExternalSeatId(1L, 100L))
                .thenReturn(Optional.of(existingSeat));

        when(existingSeat.isSameInfo(performance, 100L, "A", "1", 1))
                .thenReturn(true);

        // when
        seatService.syncSeats(performance, List.of(ticketServerSeat));

        // then
        verify(seatRepository).findByPerformanceIdAndExternalSeatId(1L, 100L);
        verify(existingSeat, never()).update(anyString(), anyString(), anyInt());
        verify(seatRepository, never()).save(any(Seat.class));
    }
}