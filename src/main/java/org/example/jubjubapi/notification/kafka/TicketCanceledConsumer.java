package org.example.jubjubapi.notification.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.global.config.KafkaConfig;
import org.example.jubjubapi.notification.event.TicketCanceledEvent;
import org.example.jubjubapi.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Kafka 메시지 수신 입구. 판단 로직 없이 서비스에 위임만 한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketCanceledConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaConfig.TICKET_CANCELED_TOPIC, groupId = "jupjup-notification")
    public void handle(TicketCanceledEvent event) {
        log.info("취소표 이벤트 수신. eventId={}, ticketId={}, performanceId={}",
                event.eventId(), event.ticketId(), event.performanceId());
        notificationService.notifySubscribers(event);
    }
}