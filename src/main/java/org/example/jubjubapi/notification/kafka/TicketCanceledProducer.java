package org.example.jubjubapi.notification.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.global.config.KafkaConfig;
import org.example.jubjubapi.notification.event.TicketCanceledEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketCanceledProducer {

    private final KafkaTemplate<String, TicketCanceledEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(TicketCanceledEvent event) {
        // key = ticketId → 같은 티켓 이벤트는 같은 파티션 (순서 보장)
        kafkaTemplate.send(KafkaConfig.TICKET_CANCELED_TOPIC,
                        String.valueOf(event.ticketId()),
                        event)
                .whenComplete((result, ex) -> {
                    // send()는 비동기라 전송 실패는 여기서만 잡힌다.
                    // @TransactionalEventListener는 예외를 삼키므로 로그가 유일한 단서.
                    if (ex != null) {
                        log.error("취소표 이벤트 Kafka 발행 실패. eventId={}, ticketId={}",
                                event.eventId(), event.ticketId(), ex);
                    }
                });
    }
}