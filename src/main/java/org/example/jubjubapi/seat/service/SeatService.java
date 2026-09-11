package org.example.jubjubapi.seat.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.performance.entity.Performance;
import org.example.jubjubapi.seat.dto.TicketServerSeat;
import org.example.jubjubapi.seat.entity.Seat;
import org.example.jubjubapi.seat.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    @Transactional
    public void syncSeats(Performance savedPerformance, List<TicketServerSeat> seats) {
        for (TicketServerSeat seat : seats) {
            seatRepository.findByPerformanceIdAndExternalSeatId(savedPerformance.getId(), seat.getId())
                    .ifPresentOrElse(
                            existingSeat -> {
                                if (!existingSeat.isSameInfo(
                                        savedPerformance,
                                        seat.getId(),
                                        seat.getSection(),
                                        seat.getSeatRow(),
                                        seat.getSeatNumber()
                                )) {
                                    existingSeat.update(
                                            seat.getSection(),
                                            seat.getSeatRow(),
                                            seat.getSeatNumber()
                                    );
                                }
                            },
                            () -> seatRepository.save(
                                    new Seat(
                                            savedPerformance,
                                            seat.getId(),
                                            seat.getSection(),
                                            seat.getSeatRow(),
                                            seat.getSeatNumber()
                                    )
                            )
                    );
        }
    }
}
