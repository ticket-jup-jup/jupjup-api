package org.example.jubjubapi.performance.repository;

import org.example.jubjubapi.performance.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    Optional<Performance> findByExternalPerformanceId(Long externalPerformanceId);

    List<Performance> findAllByProgramIdAndDeletedAtIsNullAndExternalPerformanceIdNotIn(Long id, Set<Long> externalPerformanceIds);
}