package org.example.jubjubapi.notification.kafka;

import org.example.jubjubapi.notification.event.TicketCanceledEvent;
import org.example.jubjubapi.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketCanceledConsumerTest {

    @Mock
    NotificationService notificationService;
    @InjectMocks
    TicketCanceledConsumer consumer;

    @Test
    @DisplayName("메시지를 받으면 NotificationService에 그대로 위임한다")
    void handle_delegates() {
        TicketCanceledEvent event = new TicketCanceledEvent("evt-1", 10L, 10L, 3L, LocalDateTime.now());
        consumer.handle(event);
        verify(notificationService).notifySubscribers(event);
    }
}
