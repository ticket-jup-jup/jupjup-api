package org.example.jubjubapi.reservation.repository;

import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query(
            "select r " +
                    "from Reservation r " +
                    "join fetch r.ticket " +
                    "where r.user.id = :userId"
    )
    List<Reservation> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(
            "select r " +
                    "from Reservation r " +
                    "join fetch r.ticket " +
                    "where r.id = :id"
    )
    Optional<Reservation> findByIdWithTicket(@Param("id") Long reservationId);

    List<Reservation> findAllByStatusAndExpiresAtLessThanEqual(ReservationStatus reservationStatus, LocalDateTime now);
}
