# 🎫 줍줍 (JupJup)

> 취소표를 실시간으로 감지하고, 사용자에게 알림을 제공하여  
> 빠르게 취소표를 예매할 수 있도록 도와주는 서비스

---

## 📌 프로젝트 소개

공연, 스포츠 경기, 기차 등의 예매에서 원하는 좌석이 매진된 경우  
사용자가 직접 반복적으로 예매 사이트를 확인해야 하는 불편함이 있습니다.

**줍줍**은 사용자가 원하는 티켓을 등록해두면 취소표 발생 여부를 감지하고,  
취소표가 발생했을 때 사용자에게 실시간으로 알림을 전달하여  
빠르게 예매할 수 있도록 지원하는 서비스입니다.

### 핵심 기능

- 회원가입 / 로그인
- 예매 서버 계정 연동
- 공연 및 티켓 조회
- 원하는 티켓 취소표 등록
- 취소표 발생 감지
- 취소표 실시간 알림
- 취소표 선착순 예매
- 결제
- 예약 내역 조회
- 예매/결제 상태 관리

---

# 🗄️ ERD

## 줍줍 서버 ERD

```mermaid
erDiagram

    USER {
        BIGINT id PK "사용자 ID"
        VARCHAR email UK "이메일"
        VARCHAR password "비밀번호"
        VARCHAR name "사용자 이름"
        VARCHAR status "회원 상태"
        DATETIME deleted_at "탈퇴일시"
        DATETIME created_at "가입일시"
        DATETIME updated_at "수정일시"
    }

    TICKET_SERVER_ACCOUNT {
        BIGINT id PK "연동 ID"
        BIGINT user_id FK "사용자 ID"
        BIGINT external_user_id "예매 서버 사용자 ID"
        VARCHAR access_token "예매 서버 인증 토큰"
        DATETIME created_at "연동일시"
        DATETIME updated_at "수정일시"
    }

    TICKET {
        BIGINT id PK "줍줍 티켓 ID"
        BIGINT external_ticket_id "예매 서버 티켓 ID"
        BIGINT performance_id "회차 ID"
        VARCHAR program_name "프로그램명"
        DATETIME start_at "시작일시"
        VARCHAR venue "장소"
        VARCHAR seat_grade "좌석 등급"
        VARCHAR section "구역"
        VARCHAR row_number "열"
        VARCHAR seat_number "좌석 번호"
        DECIMAL price "가격"
        VARCHAR status "티켓 상태"
        DATETIME created_at "생성일시"
        DATETIME updated_at "수정일시"
    }

    TICKET_WATCH {
        BIGINT id PK "취소표 알림 ID"
        BIGINT user_id FK "사용자 ID"
        BIGINT ticket_id FK "티켓 ID"
        VARCHAR status "알림 설정 상태"
        DATETIME created_at "생성일시"
        DATETIME updated_at "수정일시"
    }

    NOTIFICATION {
        BIGINT id PK "알림 ID"
        BIGINT user_id FK "사용자 ID"
        BIGINT ticket_id FK "티켓 ID"
        BIGINT watch_id FK "알림 설정 ID"
        VARCHAR type "알림 유형"
        VARCHAR message "알림 내용"
        VARCHAR status "발송 상태"
        DATETIME sent_at "발송일시"
        DATETIME created_at "생성일시"
    }

    RESERVATION {
        BIGINT id PK "예약 ID"
        BIGINT user_id FK "사용자 ID"
        BIGINT ticket_id FK "티켓 ID"
        BIGINT external_reservation_id "예매 서버 예약 ID"
        VARCHAR status "예약 상태"
        DATETIME expires_at "예약 만료일시"
        DATETIME created_at "예약일시"
        DATETIME updated_at "수정일시"
    }

    PAYMENT {
        BIGINT id PK "결제 ID"
        BIGINT reservation_id FK "예약 ID"
        DECIMAL amount "결제 금액"
        VARCHAR payment_method "결제 수단"
        VARCHAR status "결제 상태"
        DATETIME paid_at "결제일시"
        DATETIME created_at "결제 생성일시"
    }

    INBOX_EVENT {
        BIGINT id PK "Inbox 이벤트 ID"
        VARCHAR event_id UK "이벤트 ID"
        VARCHAR event_type "이벤트 유형"
        VARCHAR aggregate_type "대상 유형"
        BIGINT aggregate_id "대상 ID"
        JSON payload "이벤트 데이터"
        DATETIME processed_at "처리일시"
        DATETIME created_at "생성일시"
    }

    USER ||--|| TICKET_SERVER_ACCOUNT : "계정 연동"

    USER ||--o{ TICKET_WATCH : "알림 등록"

    TICKET ||--o{ TICKET_WATCH : "관심 티켓"

    TICKET_WATCH ||--o{ NOTIFICATION : "알림 생성"

    USER ||--o{ NOTIFICATION : "알림 수신"

    TICKET ||--o{ NOTIFICATION : "알림 대상"

    USER ||--o{ RESERVATION : "예약"

    TICKET ||--o{ RESERVATION : "예약 대상"

    RESERVATION ||--o| PAYMENT : "결제"
```

---

# 🎟️ 가상 예매 서버 ERD

```mermaid
erDiagram

    USER {
        BIGINT id PK "사용자 ID"
        VARCHAR email UK "이메일"
        VARCHAR password "비밀번호"
        VARCHAR name "사용자 이름"
        DATETIME created_at "가입일시"
        DATETIME updated_at "수정일시"
    }

    PROGRAM {
        BIGINT id PK "프로그램 ID"
        VARCHAR name "프로그램명"
        VARCHAR type "프로그램 유형"
        VARCHAR description "프로그램 설명"
        DATETIME created_at "생성일시"
        DATETIME updated_at "수정일시"
    }

    PERFORMANCE {
        BIGINT id PK "회차 ID"
        BIGINT program_id FK "프로그램 ID"
        DATETIME start_at "시작일시"
        DATETIME end_at "종료일시"
        VARCHAR venue "장소"
        VARCHAR status "회차 상태"
        DATETIME created_at "생성일시"
        DATETIME updated_at "수정일시"
    }

    SEAT {
        BIGINT id PK "좌석 ID"
        VARCHAR seat_grade "좌석 등급"
        VARCHAR section "구역"
        VARCHAR row_number "열"
        VARCHAR seat_number "좌석 번호"
    }

    TICKET {
        BIGINT id PK "티켓 ID"
        BIGINT performance_id FK "회차 ID"
        BIGINT seat_id FK "좌석 ID"
        DECIMAL price "티켓 가격"
        VARCHAR status "티켓 상태"
        DATETIME created_at "생성일시"
        DATETIME updated_at "수정일시"
    }

    RESERVATION {
        BIGINT id PK "예약 ID"
        BIGINT user_id FK "사용자 ID"
        BIGINT ticket_id FK "티켓 ID"
        VARCHAR status "예약 상태"
        DATETIME expires_at "예약 만료일시"
        DATETIME created_at "예약일시"
        DATETIME updated_at "수정일시"
    }

    PAYMENT {
        BIGINT id PK "결제 ID"
        BIGINT reservation_id FK "예약 ID"
        DECIMAL amount "결제 금액"
        VARCHAR payment_method "결제 수단"
        VARCHAR status "결제 상태"
        DATETIME paid_at "결제일시"
        DATETIME created_at "결제 생성일시"
    }

    OUTBOX_EVENT {
        BIGINT id PK "이벤트 ID"
        VARCHAR aggregate_type "대상 유형"
        BIGINT aggregate_id "대상 ID"
        VARCHAR event_type "이벤트 유형"
        JSON payload "이벤트 데이터"
        VARCHAR status "발행 상태"
        INT retry_count "재시도 횟수"
        DATETIME published_at "발행일시"
        DATETIME created_at "생성일시"
    }

    USER ||--o{ RESERVATION : "예약"

    PROGRAM ||--o{ PERFORMANCE : "회차"

    PERFORMANCE ||--o{ TICKET : "티켓"

    SEAT ||--o{ TICKET : "좌석"

    TICKET ||--o{ RESERVATION : "예약 대상"

    RESERVATION ||--o| PAYMENT : "결제"
```
