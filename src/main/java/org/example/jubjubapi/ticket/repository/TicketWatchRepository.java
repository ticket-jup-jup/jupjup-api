package org.example.jubjubapi.ticket.repository;

import jakarta.persistence.LockModeType;
import org.example.jubjubapi.ticket.entity.TicketWatch;
import org.example.jubjubapi.ticket.entity.TicketWatchStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface TicketWatchRepository extends JpaRepository<TicketWatch, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketWatch> findByUser_IdAndTicket_Id(Long userId, Long ticketId);

    @EntityGraph(attributePaths = "ticket")
    List<TicketWatch> findByUser_IdAndStatus(
            Long userId, TicketWatchStatus status, Pageable pageable);

    // 사용자 ID를 조건에 포함해서 다른 사람의 구독에는 접근하지 않는다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketWatch> findByIdAndUser_Id(Long watchId, Long userId);

    boolean existsByTicket_Id(Long ticketId);

    /*이후 kafka 알림 처리용*/
    @EntityGraph(attributePaths = {"user", "ticket"})
    List<TicketWatch> findByTicket_IdAndStatus(
            Long ticketId, TicketWatchStatus status);
}