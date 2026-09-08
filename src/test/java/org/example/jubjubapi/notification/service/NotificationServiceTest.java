package org.example.jubjubapi.notification.service;

import org.example.jubjubapi.notification.entity.Notification;
import org.example.jubjubapi.notification.event.TicketCanceledEvent;
import org.example.jubjubapi.notification.repository.NotificationRepository;
import org.example.jubjubapi.ticket.entity.PerformanceWatch;               // 김씨 (이름 확인)
import org.example.jubjubapi.ticket.entity.PerformanceWatchStatus;         // 김씨 (이름 확인)
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.repository.PerformanceWatchRepository; // 김씨 (이름 확인)
import org.example.jubjubapi.ticket.repository.TicketRepository;
import org.example.jubjubapi.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock TicketRepository ticketRepository;
    @Mock PerformanceWatchRepository performanceWatchRepository;
    @InjectMocks NotificationService notificationService;

    private final TicketCanceledEvent event =
            new TicketCanceledEvent("evt-1", 10L, 10L, 3L, LocalDateTime.now());

    private PerformanceWatch watchOf(Long userId) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        PerformanceWatch watch = mock(PerformanceWatch.class);
        when(watch.getUser()).thenReturn(user);
        return watch;
    }

    @Test
    @DisplayName("구독자 2명 중 1명은 이미 알림이 있으면 1건만 생성한다")
    void notifySubscribers_skipsDuplicate() {
        // given
        PerformanceWatch w1 = watchOf(1L);
        PerformanceWatch w2 = watchOf(2L);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(mock(Ticket.class)));
        when(performanceWatchRepository.findByPerformance_IdAndStatus(3L, PerformanceWatchStatus.ACTIVE))
                .thenReturn(List.of(w1, w2));
        when(notificationRepository.existsByEventIdAndUser_Id("evt-1", 1L)).thenReturn(true);   // 이미 있음
        when(notificationRepository.existsByEventIdAndUser_Id("evt-1", 2L)).thenReturn(false);

        // when
        notificationService.notifySubscribers(event);

        // then
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("구독자가 없으면 아무것도 저장하지 않는다")
    void notifySubscribers_noWatchers() {
        // given
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(mock(Ticket.class)));
        when(performanceWatchRepository.findByPerformance_IdAndStatus(3L, PerformanceWatchStatus.ACTIVE))
                .thenReturn(List.of());

        // when
        notificationService.notifySubscribers(event);

        // then
        verify(notificationRepository, never()).save(any());
    }
}
