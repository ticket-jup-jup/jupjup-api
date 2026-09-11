package org.example.jubjubapi.ticket.performance.service;

import org.example.jubjubapi.performancewatch.dto.PerformanceWatchResponse;
import org.example.jubjubapi.performance.entity.Performance;
import org.example.jubjubapi.performance.entity.PerformanceStatus;
import org.example.jubjubapi.performancewatch.entity.PerformanceWatch;
import org.example.jubjubapi.performancewatch.entity.PerformanceWatchStatus;
import org.example.jubjubapi.performance.repository.PerformanceRepository;
import org.example.jubjubapi.performancewatch.repository.PerformanceWatchRepository;
import org.example.jubjubapi.performancewatch.service.PerformanceWatchService;
import org.example.jubjubapi.ticket.exception.TicketErrorCode;
import org.example.jubjubapi.ticket.exception.TicketException;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("회차 알림 구독 서비스 단위 테스트")
class PerformanceWatchServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long PERFORMANCE_ID = 10L;
    private static final Long WATCH_ID = 3L;

    @Mock
    private PerformanceRepository performanceRepository;

    @Mock
    private PerformanceWatchRepository performanceWatchRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PerformanceWatchService performanceWatchService;

    private User user;
    private Performance performance;

    @BeforeEach
    void setUp() {
        user = User.create(
                "test@example.com",
                "encoded-password",
                "사용자"
        );
        ReflectionTestUtils.setField(user, "id", USER_ID);

        performance = new Performance(
                PERFORMANCE_ID,
                100L,
                LocalDateTime.of(2026, 10, 1, 19, 0),
                LocalDateTime.of(2026, 10, 1, 21, 0),
                "공연장",
                PerformanceStatus.UPCOMING,
                "테스트 공연"
        );
    }

    @Test
    @DisplayName("처음 구독하면 ACTIVE 상태의 회차 구독을 저장한다")
    void createWatch_success() {
        stubActiveUser();

        when(performanceRepository.findById(PERFORMANCE_ID))
                .thenReturn(Optional.of(performance));

        when(performanceWatchRepository
                .findByUser_IdAndPerformance_Id(
                        USER_ID,
                        PERFORMANCE_ID
                ))
                .thenReturn(Optional.empty());

        when(performanceWatchRepository.saveAndFlush(
                any(PerformanceWatch.class)
        )).thenAnswer(invocation -> {
            PerformanceWatch saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", WATCH_ID);
            return saved;
        });

        PerformanceWatchResponse response =
                performanceWatchService.createWatch(
                        USER_ID,
                        PERFORMANCE_ID
                );

        assertAll(
                () -> assertEquals(WATCH_ID, response.getId()),
                () -> assertEquals(
                        PERFORMANCE_ID,
                        response.getPerformanceId()
                ),
                () -> assertEquals(
                        PerformanceWatchStatus.ACTIVE,
                        response.getStatus()
                )
        );

        ArgumentCaptor<PerformanceWatch> watchCaptor =
                ArgumentCaptor.forClass(PerformanceWatch.class);

        verify(performanceWatchRepository)
                .saveAndFlush(watchCaptor.capture());

        PerformanceWatch savedWatch = watchCaptor.getValue();

        assertAll(
                () -> assertSame(user, savedWatch.getUser()),
                () -> assertSame(
                        performance,
                        savedWatch.getPerformance()
                ),
                () -> assertTrue(savedWatch.isActive())
        );
    }

    @Test
    @DisplayName("이미 활성화된 동일 회차 구독은 중복 생성할 수 없다")
    void createWatch_duplicateActiveWatch() {
        stubActiveUser();

        when(performanceRepository.findById(PERFORMANCE_ID))
                .thenReturn(Optional.of(performance));

        PerformanceWatch activeWatch =
                PerformanceWatch.create(user, performance);

        when(performanceWatchRepository
                .findByUser_IdAndPerformance_Id(
                        USER_ID,
                        PERFORMANCE_ID
                ))
                .thenReturn(Optional.of(activeWatch));

        expectError(
                TicketErrorCode.WATCH_ALREADY_EXISTS,
                () -> performanceWatchService.createWatch(
                        USER_ID,
                        PERFORMANCE_ID
                )
        );

        verify(performanceWatchRepository, never())
                .saveAndFlush(any(PerformanceWatch.class));
    }

    @Test
    @DisplayName("비활성 구독을 다시 신청하면 기존 ID를 유지하고 재활성화한다")
    void createWatch_reactivateInactiveWatch() {
        stubActiveUser();

        when(performanceRepository.findById(PERFORMANCE_ID))
                .thenReturn(Optional.of(performance));

        PerformanceWatch inactiveWatch =
                PerformanceWatch.create(user, performance);
        ReflectionTestUtils.setField(
                inactiveWatch,
                "id",
                WATCH_ID
        );
        inactiveWatch.deactivate();

        when(performanceWatchRepository
                .findByUser_IdAndPerformance_Id(
                        USER_ID,
                        PERFORMANCE_ID
                ))
                .thenReturn(Optional.of(inactiveWatch));

        when(performanceWatchRepository.saveAndFlush(inactiveWatch))
                .thenReturn(inactiveWatch);

        PerformanceWatchResponse response =
                performanceWatchService.createWatch(
                        USER_ID,
                        PERFORMANCE_ID
                );

        assertAll(
                () -> assertEquals(WATCH_ID, response.getId()),
                () -> assertEquals(
                        PerformanceWatchStatus.ACTIVE,
                        response.getStatus()
                ),
                () -> assertTrue(inactiveWatch.isActive())
        );

        verify(performanceWatchRepository)
                .saveAndFlush(inactiveWatch);
    }

    @Test
    @DisplayName("존재하지 않는 회차는 구독할 수 없다")
    void createWatch_performanceNotFound() {
        stubActiveUser();

        when(performanceRepository.findById(PERFORMANCE_ID))
                .thenReturn(Optional.empty());

        expectError(
                TicketErrorCode.PERFORMANCE_NOT_FOUND,
                () -> performanceWatchService.createWatch(
                        USER_ID,
                        PERFORMANCE_ID
                )
        );

        verifyNoInteractions(performanceWatchRepository);
    }

    @Test
    @DisplayName("회차 ID가 0 이하면 회차 저장소를 조회하지 않는다")
    void createWatch_invalidPerformanceId() {
        stubActiveUser();

        expectError(
                TicketErrorCode.INVALID_ID,
                () -> performanceWatchService.createWatch(
                        USER_ID,
                        0L
                )
        );

        verifyNoInteractions(
                performanceRepository,
                performanceWatchRepository
        );
    }

    @Test
    @DisplayName("인증 사용자 ID가 없으면 저장소에 접근하지 않는다")
    void createWatch_authenticationRequired() {
        expectError(
                TicketErrorCode.AUTHENTICATION_REQUIRED,
                () -> performanceWatchService.createWatch(
                        null,
                        PERFORMANCE_ID
                )
        );

        verifyNoInteractions(
                userRepository,
                performanceRepository,
                performanceWatchRepository
        );
    }

    @Test
    @DisplayName("탈퇴한 사용자는 회차를 구독할 수 없다")
    void createWatch_unavailableUser() {
        user.withdraw(LocalDateTime.now());

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        expectError(
                TicketErrorCode.USER_UNAVAILABLE,
                () -> performanceWatchService.createWatch(
                        USER_ID,
                        PERFORMANCE_ID
                )
        );

        verifyNoInteractions(
                performanceRepository,
                performanceWatchRepository
        );
    }

    @Test
    @DisplayName("동시 구독으로 유니크 제약 충돌이 나면 WATCH_CONFLICT로 변환한다")
    void createWatch_dataIntegrityViolation() {
        stubActiveUser();

        when(performanceRepository.findById(PERFORMANCE_ID))
                .thenReturn(Optional.of(performance));

        when(performanceWatchRepository
                .findByUser_IdAndPerformance_Id(
                        USER_ID,
                        PERFORMANCE_ID
                ))
                .thenReturn(Optional.empty());

        when(performanceWatchRepository.saveAndFlush(
                any(PerformanceWatch.class)
        )).thenThrow(
                new DataIntegrityViolationException(
                        "unique constraint"
                )
        );

        expectError(
                TicketErrorCode.WATCH_CONFLICT,
                () -> performanceWatchService.createWatch(
                        USER_ID,
                        PERFORMANCE_ID
                )
        );
    }

    @Test
    @DisplayName("내 회차 구독을 상태와 페이징 조건으로 조회한다")
    void getMyWatches_success() {
        stubActiveUser();

        PerformanceWatch watch = createWatch(WATCH_ID);

        when(performanceWatchRepository.findByUser_IdAndStatus(
                eq(USER_ID),
                eq(PerformanceWatchStatus.ACTIVE),
                any(Pageable.class)
        )).thenReturn(List.of(watch));

        List<PerformanceWatchResponse> responses =
                performanceWatchService.getMyWatches(
                        USER_ID,
                        PerformanceWatchStatus.ACTIVE,
                        1,
                        5
                );

        assertAll(
                () -> assertEquals(1, responses.size()),
                () -> assertEquals(
                        WATCH_ID,
                        responses.get(0).getId()
                ),
                () -> assertEquals(
                        PERFORMANCE_ID,
                        responses.get(0).getPerformanceId()
                ),
                () -> assertEquals(
                        PerformanceWatchStatus.ACTIVE,
                        responses.get(0).getStatus()
                )
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(performanceWatchRepository)
                .findByUser_IdAndStatus(
                        eq(USER_ID),
                        eq(PerformanceWatchStatus.ACTIVE),
                        pageableCaptor.capture()
                );

        Pageable pageable = pageableCaptor.getValue();
        Sort.Order idOrder =
                pageable.getSort().getOrderFor("id");

        assertAll(
                () -> assertEquals(1, pageable.getPageNumber()),
                () -> assertEquals(5, pageable.getPageSize()),
                () -> assertNotNull(idOrder),
                () -> assertEquals(
                        Sort.Direction.DESC,
                        idOrder.getDirection()
                )
        );
    }

    @Test
    @DisplayName("구독 상태가 null이면 목록을 조회하지 않는다")
    void getMyWatches_invalidStatus() {
        stubActiveUser();

        expectError(
                TicketErrorCode.INVALID_WATCH_STATUS,
                () -> performanceWatchService.getMyWatches(
                        USER_ID,
                        null,
                        0,
                        20
                )
        );

        verify(performanceWatchRepository, never())
                .findByUser_IdAndStatus(
                        anyLong(),
                        any(PerformanceWatchStatus.class),
                        any(Pageable.class)
                );
    }

    @Test
    @DisplayName("잘못된 페이징이면 구독 목록을 조회하지 않는다")
    void getMyWatches_invalidPagination() {
        stubActiveUser();

        expectError(
                TicketErrorCode.INVALID_PAGINATION,
                () -> performanceWatchService.getMyWatches(
                        USER_ID,
                        PerformanceWatchStatus.ACTIVE,
                        -1,
                        20
                )
        );

        verify(performanceWatchRepository, never())
                .findByUser_IdAndStatus(
                        anyLong(),
                        any(PerformanceWatchStatus.class),
                        any(Pageable.class)
                );
    }

    @Test
    @DisplayName("본인의 회차 구독을 해제하면 INACTIVE 상태가 된다")
    void deactivateWatch_success() {
        stubActiveUser();

        PerformanceWatch watch = createWatch(WATCH_ID);

        when(performanceWatchRepository.findByIdAndUser_Id(
                WATCH_ID,
                USER_ID
        )).thenReturn(Optional.of(watch));

        performanceWatchService.deactivateWatch(
                USER_ID,
                WATCH_ID
        );

        assertAll(
                () -> assertFalse(watch.isActive()),
                () -> assertEquals(
                        PerformanceWatchStatus.INACTIVE,
                        watch.getStatus()
                )
        );

        verify(performanceWatchRepository, never())
                .delete(any(PerformanceWatch.class));
    }

    @Test
    @DisplayName("이미 해제된 구독을 다시 해제해도 성공한다")
    void deactivateWatch_idempotent() {
        stubActiveUser();

        PerformanceWatch watch = createWatch(WATCH_ID);
        watch.deactivate();

        when(performanceWatchRepository.findByIdAndUser_Id(
                WATCH_ID,
                USER_ID
        )).thenReturn(Optional.of(watch));

        assertDoesNotThrow(
                () -> performanceWatchService.deactivateWatch(
                        USER_ID,
                        WATCH_ID
                )
        );

        assertEquals(
                PerformanceWatchStatus.INACTIVE,
                watch.getStatus()
        );

        verify(performanceWatchRepository, never())
                .delete(any(PerformanceWatch.class));
    }

    @Test
    @DisplayName("본인 소유가 아닌 구독은 해제할 수 없다")
    void deactivateWatch_notFoundForUser() {
        stubActiveUser();

        when(performanceWatchRepository.findByIdAndUser_Id(
                WATCH_ID,
                USER_ID
        )).thenReturn(Optional.empty());

        expectError(
                TicketErrorCode.WATCH_NOT_FOUND,
                () -> performanceWatchService.deactivateWatch(
                        USER_ID,
                        WATCH_ID
                )
        );
    }

    @Test
    @DisplayName("구독 ID가 0 이하면 구독 저장소를 조회하지 않는다")
    void deactivateWatch_invalidWatchId() {
        stubActiveUser();

        expectError(
                TicketErrorCode.INVALID_ID,
                () -> performanceWatchService.deactivateWatch(
                        USER_ID,
                        0L
                )
        );

        verify(performanceWatchRepository, never())
                .findByIdAndUser_Id(anyLong(), anyLong());
    }

    private void stubActiveUser() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
    }

    private PerformanceWatch createWatch(Long id) {
        PerformanceWatch watch =
                PerformanceWatch.create(user, performance);

        ReflectionTestUtils.setField(watch, "id", id);
        return watch;
    }

    private void expectError(
            TicketErrorCode expected,
            Executable executable
    ) {
        TicketException exception =
                assertThrows(TicketException.class, executable);

        assertAll(
                () -> assertEquals(
                        expected.getCode(),
                        exception.getCode()
                ),
                () -> assertEquals(
                        expected.getStatus(),
                        exception.getStatus()
                ),
                () -> assertEquals(
                        expected.getMessage(),
                        exception.getMessage()
                )
        );
    }
}