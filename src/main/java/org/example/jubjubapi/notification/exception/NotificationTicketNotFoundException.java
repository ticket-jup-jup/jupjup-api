package org.example.jubjubapi.notification.exception;

import org.example.jubjubapi.global.exception.ServiceException;
import org.springframework.http.HttpStatus;

public class NotificationTicketNotFoundException extends ServiceException {
    public NotificationTicketNotFoundException(Long ticketId) {
        super(HttpStatus.NOT_FOUND, "NOTIFICATION_TICKET_NOT_FOUND",
                "알림 대상 티켓을 찾을 수 없습니다. ticketId=" + ticketId);
    }
}
