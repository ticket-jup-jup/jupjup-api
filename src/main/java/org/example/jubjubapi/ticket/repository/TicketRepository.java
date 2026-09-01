package org.example.jubjubapi.ticket.repository;

import org.example.jubjubapi.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
