package org.example.jubjubapi.reservation.repository;

import org.example.jubjubapi.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
