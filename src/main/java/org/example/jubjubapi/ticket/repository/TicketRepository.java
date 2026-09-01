package org.example.jubjubapi.ticket.repository;

import jakarta.persistence.LockModeType;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByExternalTicketId(Long externalTicketId);

    /*이후 webhook,polling 용*/
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.externalTicketId = :externalTicketId")
    Optional<Ticket> findByExternalTicketIdForUpdate(
            @Param("externalTicketId") Long externalTicketId);

    @Query("""
            select t from Ticket t
            where (:performanceId is null or t.performanceId = :performanceId)
              and (:status is null or t.status = :status)
            """)
    List<Ticket> search(@Param("performanceId") Long performanceId,
                        @Param("status") TicketStatus status, Pageable pageable);

    // 구독 생성/재활성화와 티켓 삭제가 같은 티켓에 대해 동시에 진행되지 않게 한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :ticketId")
    Optional<Ticket> findByIdForUpdate(@Param("ticketId") Long ticketId);

    // 예약 기능 구현이 아니라 티켓 삭제 전 참조 확인만 수행한다.
    @Query(value = "select count(*) from reservations where ticket_id = :ticketId",
            nativeQuery = true)
    long countReservationReferences(@Param("ticketId") Long ticketId);

    // 기존 Reservation은 ticketId 숫자만 보관한다. DB FK가 없으면 동시 예약을
    // 사전 조회만으로 보호할 수 없으므로 물리 삭제를 허용하지 않는다. MySQL 8 기준.
    @Query(value = """
            select count(*)
            from information_schema.KEY_COLUMN_USAGE k
            join information_schema.REFERENTIAL_CONSTRAINTS r
              on r.CONSTRAINT_SCHEMA = k.CONSTRAINT_SCHEMA
             and r.CONSTRAINT_NAME = k.CONSTRAINT_NAME
             and r.TABLE_NAME = k.TABLE_NAME
            where k.TABLE_SCHEMA = database()
              and k.TABLE_NAME = 'reservations'
              and k.COLUMN_NAME = 'ticket_id'
              and k.REFERENCED_TABLE_SCHEMA = database()
              and k.REFERENCED_TABLE_NAME = 'ticket'
              and k.REFERENCED_COLUMN_NAME = 'id'
              and r.DELETE_RULE in ('RESTRICT', 'NO ACTION')
            """, nativeQuery = true)
    long countRestrictiveReservationForeignKeys();
}
