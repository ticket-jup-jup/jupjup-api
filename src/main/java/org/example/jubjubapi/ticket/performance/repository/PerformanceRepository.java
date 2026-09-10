package org.example.jubjubapi.ticket.performance.repository;

import org.example.jubjubapi.ticket.performance.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository
        extends JpaRepository<Performance, Long> {
}