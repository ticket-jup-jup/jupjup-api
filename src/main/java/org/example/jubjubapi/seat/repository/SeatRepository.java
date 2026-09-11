package org.example.jubjubapi.seat.repository;

import org.example.jubjubapi.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
