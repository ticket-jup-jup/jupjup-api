package org.example.jubjubapi.ticket.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TicketErrorCode {
    //NOTFOUND
    WATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "WATCH_NOT_FOUND", "내 구독을 찾을 수 없습니다."),
    TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "TICKET_NOT_FOUND", "티켓을 찾을 수 없습니다."),
    //CONFLICT
    WATCH_ALREADY_EXISTS(HttpStatus.CONFLICT,"WATCH_ALREADY_EXISTS","이미 구독 중인 티켓입니다."),
    WATCH_CONFLICT(HttpStatus.CONFLICT,"WATCH_CONFLICT", "구독 저장 중 데이터 충돌이 발생했습니다."),
    TICKET_IN_USE(HttpStatus.CONFLICT,"TICKET_IN_USE", "구독·알림·예약이 연결된 티켓은 삭제할 수 없습니다."),
    TICKET_DELETE_NOT_READY(HttpStatus.CONFLICT,"TICKET_DELETE_NOT_READY","예약 참조를 보호하는 DB 외래키 설정 후 티켓을 삭제할 수 있습니다."),
    TICKET_IN_USE_FK(HttpStatus.CONFLICT,"TICKET_IN_USE", "다른 데이터에서 참조 중인 티켓은 삭제할 수 없습니다."),
    TICKET_NOT_AVAILABLE(HttpStatus.CONFLICT, "TICKET_NOT_AVAILABLE", "예약할 수 없는 티켓입니다."),
    //BAD_REQUEST
    INVALID_PAGINATION(HttpStatus.BAD_REQUEST,"INVALID_PAGINATION",
            "page는 0 이상, size는 1~100이어야 하며 조회 범위가 너무 크면 안 됩니다."),
    INVALID_ID(HttpStatus.BAD_REQUEST,"INVALID_ID",
            "ID는 1 이상이어야 합니다."),
    INVALID_WATCH_STATUS(HttpStatus.BAD_REQUEST, "INVALID_WATCH_STATUS",
            "구독 상태는 필수입니다."),
    INVALID_TICKET_PRICE(HttpStatus.BAD_REQUEST,
            "INVALID_TICKET_PRICE",
            "티켓 가격은 필수이며 0 이상이어야 합니다."),
    //UNAUTHORIZED
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED",
            "로그인이 필요합니다."),
    USER_UNAVAILABLE(HttpStatus.UNAUTHORIZED,"USER_UNAVAILABLE", "사용 가능한 회원이 아닙니다."),
    //FORBIDDEN
    ADMIN_REQUIRED(HttpStatus.FORBIDDEN, "ADMIN_REQUIRED",
            "티켓 삭제는 관리자만 할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    TicketErrorCode(HttpStatus status,String code, String message){
        this.status = status;
        this.code = code;
        this.message = message;
    }


}
