package org.example.jubjubapi.ticket.performance.repository;

import jakarta.persistence.LockModeType;
import org.example.jubjubapi.ticket.performance.entity.PerformanceWatch;
import org.example.jubjubapi.ticket.performance.entity.PerformanceWatchStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface PerformanceWatchRepository extends JpaRepository<PerformanceWatch, Long>{
    // 동일 사용자 + 동일 회차 구독 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PerformanceWatch> findByUser_IdAndPerformance_Id(
            Long userId,
            Long performanceId
    );

    // 내 구독 목록 조회
    @EntityGraph(attributePaths = "performance")
    List<PerformanceWatch> findByUser_IdAndStatus(
            Long userId,
            PerformanceWatchStatus status,
            Pageable pageable
    );

    // 내 구독 하나 조회 - 다른 사용자의 구독 해제 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PerformanceWatch> findByIdAndUser_Id(
            Long watchId,
            Long userId
    );

    boolean existsByPerformance_Id(Long performanceId);


    @EntityGraph(attributePaths = {"user", "performance"})
    List<PerformanceWatch> findByPerformance_IdAndStatus(
            Long performanceId,
            PerformanceWatchStatus status
    );
}
