package org.example.jubjubapi.payment.repository;

import org.example.jubjubapi.payment.entity.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query(
            "select p " +
            "from Payment p " +
            "join fetch p.reservation r " +
            "join fetch r.ticket " +
            "where p.id = :id"
    )
    Optional<Payment> findByIdWithReservationAndTicket(@Param("id") Long id);

    @Query(
            "select p " +
            "from Payment p " +
            "join fetch p.reservation r " +
            "join fetch r.ticket " +
            "where r.user.id = :userId"
    )
    Optional<List<Payment>> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
