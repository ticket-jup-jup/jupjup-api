package org.example.jubjubapi.performance.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.performance.dto.PerformanceWatchResponse;
import org.example.jubjubapi.performance.entity.Performance;
import org.example.jubjubapi.performance.entity.PerformanceWatch;
import org.example.jubjubapi.performance.entity.PerformanceWatchStatus;
import org.example.jubjubapi.performance.repository.PerformanceRepository;
import org.example.jubjubapi.performance.repository.PerformanceWatchRepository;
import org.example.jubjubapi.ticket.exception.TicketErrorCode;
import org.example.jubjubapi.ticket.exception.TicketException;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceWatchService {

    private final PerformanceRepository performanceRepository;
    private final PerformanceWatchRepository performanceWatchRepository;
    private final UserRepository userRepository;

    // 회차 알림 구독 생성
    @Transactional
    public PerformanceWatchResponse createWatch(
            Long userId,
            Long performanceId
    ) {
        User user = findActiveUser(userId);

        requirePositiveId(performanceId);

        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() ->
                        new TicketException(TicketErrorCode.PERFORMANCE_NOT_FOUND)
                );

        PerformanceWatch watch =
                performanceWatchRepository
                        .findByUser_IdAndPerformance_Id(
                                userId,
                                performanceId
                        )
                        .orElse(null);

        // 이미 ACTIVE 상태면 중복 구독
        if (watch != null && watch.isActive()) {
            throw new TicketException(
                    TicketErrorCode.WATCH_ALREADY_EXISTS
            );
        }

        if (watch == null) {
            watch = PerformanceWatch.create(
                    user,
                    performance
            );
        } else {
            // 기존 INACTIVE 구독을 다시 활성화
            watch.activate();
        }

        try {
            return PerformanceWatchResponse.from(
                    performanceWatchRepository.saveAndFlush(watch)
            );

        } catch (DataIntegrityViolationException ex) {
            throw new TicketException(
                    TicketErrorCode.WATCH_CONFLICT
            );
        }
    }

    // 내 회차 구독 목록 조회
    public List<PerformanceWatchResponse> getMyWatches(
            Long userId,
            PerformanceWatchStatus status,
            int page,
            int size
    ) {
        findActiveUser(userId);

        if (status == null) {
            throw new TicketException(
                    TicketErrorCode.INVALID_WATCH_STATUS
            );
        }

        return performanceWatchRepository
                .findByUser_IdAndStatus(
                        userId,
                        status,
                        pageable(page, size)
                )
                .stream()
                .map(PerformanceWatchResponse::from)
                .toList();
    }

    // 회차 알림 구독 해제
    @Transactional
    public void deactivateWatch(
            Long userId,
            Long watchId
    ) {
        findActiveUser(userId);

        requirePositiveId(watchId);

        PerformanceWatch watch =
                performanceWatchRepository
                        .findByIdAndUser_Id(
                                watchId,
                                userId
                        )
                        .orElseThrow(() ->
                                new TicketException(
                                        TicketErrorCode.WATCH_NOT_FOUND
                                )
                        );

        watch.deactivate();
    }

    // 활성 사용자 조회
    private User findActiveUser(Long userId) {

        if (userId == null || userId <= 0) {
            throw new TicketException(
                    TicketErrorCode.AUTHENTICATION_REQUIRED
            );
        }

        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() ->
                        new TicketException(
                                TicketErrorCode.USER_UNAVAILABLE
                        )
                );
    }

    private void requirePositiveId(Long id) {

        if (id == null || id <= 0) {
            throw new TicketException(
                    TicketErrorCode.INVALID_ID
            );
        }
    }

    private Pageable pageable(
            int page,
            int size
    ) {
        if (
                page < 0 ||
                        size < 1 ||
                        size > 100 ||
                        (long) page * size > Integer.MAX_VALUE
        ) {
            throw new TicketException(
                    TicketErrorCode.INVALID_PAGINATION
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "id"
                )
        );
    }
}