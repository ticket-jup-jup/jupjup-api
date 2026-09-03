package org.example.jubjubapi.reservation.service;

import org.example.jubjubapi.reservation.dto.response.ReservationGetResponse;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.exception.InvalidPageRequestException;
import org.example.jubjubapi.reservation.exception.ReservationAccessDeniedException;
import org.example.jubjubapi.reservation.exception.ReservationNotFoundException;
import org.example.jubjubapi.reservation.repository.ReservationRepository;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.repository.TicketRepository;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(properties = "spring.datasource.url=jdbc:mysql://localhost:3306/jupjup_test?createDatabaseIfNotExist=true")
@DisplayName("예약 조회 테스트")
class ReservationTransactionServiceTest {

    @Autowired
    private ReservationTransactionService reservationTransactionService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    private User me;
    private User other;
    private Reservation myReservation;
    private Reservation otherReservation;

    @BeforeEach
    void setUp() {
        // 사용자 두명 생성
        me = userRepository.save(User.create("a"+System.nanoTime()+"@test.com", "test123", "me"));
        other = userRepository.save(User.create("a"+System.nanoTime()+"@test.com", "test123", "other"));

        // 나의 예약 2개
        myReservation = createReservation(me, "뮤지컬 캣츠");
        createReservation(me, "연극 햄릿");

        // 다른 사람의 예약 1개
        otherReservation = createReservation(other, "레미제라블");
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("본인 예약을 단건 조회하면 티켓 정보와 함께 반환한다.")
    void 단건_조회_성공() {
        //when
        ReservationGetResponse response = reservationTransactionService.getReservation(me.getId(), myReservation.getId());

        //then
        assertThat(response.getId()).isEqualTo(myReservation.getId());
        assertThat(response.getTicket().getProgramName()).isEqualTo("뮤지컬 캣츠");
    }

    @Test
    @DisplayName("존재하지 않는 예약을 조회하면 예외가 발생한다")
    void 단건_조회_없는_예약() {
        assertThatThrownBy(() -> reservationTransactionService.getReservation(me.getId(), 12345L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @DisplayName("다른 사용자의 예약을 조회하면 예외가 발생한다.")
    void 단건_조회_권한_없음() {
        assertThatThrownBy(() -> reservationTransactionService.getReservation(me.getId(), otherReservation.getId()))
                .isInstanceOf(ReservationAccessDeniedException.class);
    }

    @Test
    @DisplayName("페이지 크기만큼 조회된다.")
    void 목록_조회_페이징() {
        List<ReservationGetResponse> response = reservationTransactionService.getMyReservation(me.getId(), 0, 1);
        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("잘못된 페이지 요청이면 예외가 발생한다")
    void 목록_조회_페이지_검증() {
        assertThatThrownBy(() -> reservationTransactionService.getMyReservation(me.getId(), -1, 10))
                .isInstanceOf(InvalidPageRequestException.class);

        assertThatThrownBy(() -> reservationTransactionService.getMyReservation(me.getId(), 0, 0))
                .isInstanceOf(InvalidPageRequestException.class);

        assertThatThrownBy(() -> reservationTransactionService.getMyReservation(me.getId(), 0, 101))
                .isInstanceOf(InvalidPageRequestException.class);
    }

    private Reservation createReservation(User user, String programName) {
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .externalTicketId(System.nanoTime())
                .performanceId(1L)
                .programName(programName)
                .startAt(LocalDateTime.now().plusDays(30))
                .venue("테스트 공연장")
                .seatGrade("VIP")
                .price(new BigDecimal("100000.00"))
                .status(TicketStatus.AVAILABLE)
                .build());

        return reservationRepository.save(
                Reservation.create(user, ticket, LocalDateTime.now().plusMinutes(10)));
    }
}