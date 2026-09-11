package org.example.jubjubapi.seat.repository;

import org.example.jubjubapi.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findByPerformanceIdAndExternalSeatId(Long performanceId, Long externalSeatId);
}
