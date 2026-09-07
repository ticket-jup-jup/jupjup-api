package org.example.jubjubapi.ticket;

import org.example.jubjubapi.global.exception.ServiceException;
import org.example.jubjubapi.ticket.performance.entity.Performance;
import org.example.jubjubapi.ticket.performance.entity.PerformanceStatus;
import org.example.jubjubapi.ticket.performance.entity.PerformanceWatch;
import org.example.jubjubapi.ticket.performance.entity.PerformanceWatchStatus;
import org.example.jubjubapi.ticket.performance.repository.PerformanceRepository;
import org.example.jubjubapi.ticket.performance.repository.PerformanceWatchRepository;
import org.example.jubjubapi.ticket.performance.service.PerformanceWatchService;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PerformanceWatchServiceTest {

    private PerformanceRepository performances;
    private PerformanceWatchRepository watches;
    private UserRepository users;

    private PerformanceWatchService service;

    private User user;
    private Performance performance;

    @BeforeEach
    void setUp() {

        performances =
                mock(PerformanceRepository.class);

        watches =
                mock(PerformanceWatchRepository.class);

        users =
                mock(UserRepository.class);

        service = new PerformanceWatchService(
                performances,
                watches,
                users
        );

        user = User.create(
                "test@example.com",
                "encoded-password",
                "사용자"
        );

        ReflectionTestUtils.setField(
                user,
                "id",
                1L
        );

        performance = new Performance(
                10L,
                100L,
                LocalDateTime.of(
                        2026,
                        10,
                        1,
                        19,
                        0
                ),
                LocalDateTime.of(
                        2026,
                        10,
                        1,
                        21,
                        0
                ),
                "공연장",
                PerformanceStatus.UPCOMING
        );

        when(users.findById(1L))
                .thenReturn(
                        Optional.of(user)
                );

        when(performances.findById(10L))
                .thenReturn(
                        Optional.of(performance)
                );
    }
    @Test
    @DisplayName("회차 알림 구독 생성 성공")
    void createsWatchForAuthenticatedUser() {

        when(
                watches.findByUser_IdAndPerformance_Id(
                        1L,
                        10L
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                watches.saveAndFlush(
                        any(PerformanceWatch.class)
                )
        ).thenAnswer(invocation -> {

            PerformanceWatch saved =
                    invocation.getArgument(0);

            ReflectionTestUtils.setField(
                    saved,
                    "id",
                    3L
            );

            assertSame(
                    user,
                    saved.getUser()
            );

            assertSame(
                    performance,
                    saved.getPerformance()
            );

            return saved;
        });

        var response =
                service.createWatch(
                        1L,
                        10L
                );

        assertEquals(
                3L,
                response.getId()
        );

        assertEquals(
                10L,
                response.getPerformanceId()
        );

        assertEquals(
                PerformanceWatchStatus.ACTIVE,
                response.getStatus()
        );
    }

    @Test
    @DisplayName("활성 회차 구독 중복 생성 실패")
    void rejectsDuplicateActiveWatch() {

        PerformanceWatch watch =
                PerformanceWatch.create(
                        user,
                        performance
                );

        when(
                watches.findByUser_IdAndPerformance_Id(
                        1L,
                        10L
                )
        ).thenReturn(
                Optional.of(watch)
        );

        ServiceException error =
                assertThrows(
                        ServiceException.class,
                        () -> service.createWatch(
                                1L,
                                10L
                        )
                );

        assertEquals(
                "WATCH_ALREADY_EXISTS",
                error.getCode()
        );

        assertEquals(
                HttpStatus.CONFLICT,
                error.getStatus()
        );

        verify(
                watches,
                never()
        ).saveAndFlush(any());
    }

    @Test
    @DisplayName("비활성 회차 구독 재활성화 성공")
    void reusesWatchIdWhenResubscribing() {

        PerformanceWatch watch =
                PerformanceWatch.create(
                        user,
                        performance
                );

        ReflectionTestUtils.setField(
                watch,
                "id",
                3L
        );

        watch.deactivate();

        when(
                watches.findByUser_IdAndPerformance_Id(
                        1L,
                        10L
                )
        ).thenReturn(
                Optional.of(watch)
        );

        when(
                watches.saveAndFlush(watch)
        ).thenReturn(watch);

        assertEquals(
                3L,
                service.createWatch(
                        1L,
                        10L
                ).getId()
        );

        assertTrue(
                watch.isActive()
        );

        verify(watches)
                .saveAndFlush(watch);
    }

    @Test
    @DisplayName("다른 사용자 회차 구독 해제 실패")
    void cannotDeactivateAnotherUsersWatch() {

        when(
                watches.findByIdAndUser_Id(
                        30L,
                        1L
                )
        ).thenReturn(
                Optional.empty()
        );

        var error =
                assertThrows(
                        ServiceException.class,
                        () -> service.deactivateWatch(
                                1L,
                                30L
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                error.getStatus()
        );

        verify(watches)
                .findByIdAndUser_Id(
                        30L,
                        1L
                );
    }

    @Test
    @DisplayName("회차 구독 해제 멱등성 검증")
    void unsubscribeIsIdempotentAndKeepsRow() {

        PerformanceWatch watch =
                PerformanceWatch.create(
                        user,
                        performance
                );

        when(
                watches.findByIdAndUser_Id(
                        3L,
                        1L
                )
        ).thenReturn(
                Optional.of(watch)
        );

        service.deactivateWatch(
                1L,
                3L
        );

        service.deactivateWatch(
                1L,
                3L
        );

        assertFalse(
                watch.isActive()
        );

        verify(
                watches,
                never()
        ).delete(any());
    }

    @Test
    @DisplayName("비활성 사용자 회차 구독 실패")
    void unavailableUserCannotSubscribe() {

        user.withdraw(
                LocalDateTime.now()
        );

        var error =
                assertThrows(
                        ServiceException.class,
                        () -> service.createWatch(
                                1L,
                                10L
                        )
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                error.getStatus()
        );

        verify(
                performances,
                never()
        ).findById(anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 회차 구독 실패")
    void missingPerformanceReturns404() {

        when(
                performances.findById(10L)
        ).thenReturn(
                Optional.empty()
        );

        var error =
                assertThrows(
                        ServiceException.class,
                        () -> service.createWatch(
                                1L,
                                10L
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                error.getStatus()
        );

        assertEquals(
                "PERFORMANCE_NOT_FOUND",
                error.getCode()
        );

        verify(
                watches,
                never()
        ).saveAndFlush(any());
    }

    @Test
    @DisplayName("동시 회차 구독 저장 충돌 시 409")
    void concurrentSubscriptionConflictReturns409() {

        when(
                watches.findByUser_IdAndPerformance_Id(
                        1L,
                        10L
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                watches.saveAndFlush(
                        any(PerformanceWatch.class)
                )
        ).thenThrow(
                new DataIntegrityViolationException(
                        "unique constraint"
                )
        );

        var error =
                assertThrows(
                        ServiceException.class,
                        () -> service.createWatch(
                                1L,
                                10L
                        )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                error.getStatus()
        );

        assertEquals(
                "WATCH_CONFLICT",
                error.getCode()
        );
    }
}

