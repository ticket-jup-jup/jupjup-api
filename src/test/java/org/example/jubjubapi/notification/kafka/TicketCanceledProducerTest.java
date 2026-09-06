package org.example.jubjubapi.notification.kafka;

import org.example.jubjubapi.notification.event.TicketCanceledEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketCanceledProducerTest {

    @Mock
    KafkaTemplate<String, TicketCanceledEvent> kafkaTemplate;
    @InjectMocks
    TicketCanceledProducer producer;

    @Test
    @DisplayName("이벤트를 받으면 ticket-canceled 토픽에 ticketId를 key로 전송한다")
    void on_sendsToKafka() {
        // given
        TicketCanceledEvent event =
                new TicketCanceledEvent("evt-1", 10L, 10L, 3L, LocalDateTime.now());

        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // when
        producer.on(event);

        // then
        verify(kafkaTemplate).send(eq("ticket-canceled"), eq("10"), eq(event));
    }

    @Test
    @DisplayName("전송이 실패로 완료돼도 예외를 밖으로 던지지 않는다 (로그만 남김)")
    void on_logsOnFailure() {
        // given
        TicketCanceledEvent event =
                new TicketCanceledEvent("evt-1", 10L, 10L, 3L, LocalDateTime.now());
        CompletableFuture<SendResult<String, TicketCanceledEvent>> failed =
                CompletableFuture.failedFuture(new RuntimeException("broker down"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(failed);

        // when / then — 예외 없이 종료
        producer.on(event);
    }
}