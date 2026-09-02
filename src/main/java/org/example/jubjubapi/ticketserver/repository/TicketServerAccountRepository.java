package org.example.jubjubapi.ticketserver.repository;

import org.example.jubjubapi.ticketserver.entity.TicketServerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketServerAccountRepository extends JpaRepository<TicketServerAccount, Long> {

    Optional<TicketServerAccount> findByUserId(Long userId);
    boolean existsByExternalUserId(Long externalUserId);
}
