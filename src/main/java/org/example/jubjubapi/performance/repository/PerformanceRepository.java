package org.example.jubjubapi.performance.repository;

import org.example.jubjubapi.performance.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository
        extends JpaRepository<Performance, Long> {
}