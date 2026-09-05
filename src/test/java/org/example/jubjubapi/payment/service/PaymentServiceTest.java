package org.example.jubjubapi.payment.service;

import org.example.jubjubapi.payment.dto.request.PaymentCreateRequest;
import org.example.jubjubapi.payment.dto.response.PaymentCreateResponse;
import org.example.jubjubapi.payment.dto.response.PaymentGetResponse;
import org.example.jubjubapi.payment.entity.PaymentMethod;
import org.example.jubjubapi.payment.entity.PaymentStatus;
import org.example.jubjubapi.payment.repository.PaymentRepository;
import org.example.jubjubapi.reservation.entity.Reservation;
import org.example.jubjubapi.reservation.entity.ReservationStatus;
import org.example.jubjubapi.reservation.exception.ReservationNotPendingException;
import org.example.jubjubapi.reservation.repository.ReservationRepository;
import org.example.jubjubapi.ticket.client.TicketServerClient;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "spring.datasource.url=jdbc:mysql://localhost:3306/jupjup_test?createDatabaseIfNotExist=true")
class PaymentServiceTest {

    private static final BigDecimal TICKET_PRICE = new BigDecimal("10000.00");

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @MockitoBean
    private TicketServerClient ticketServerClient;

    private User me;
    private User other;
    private Reservation myReservation;
    private Reservation otherReservation;

    @BeforeEach
    void setUp() {
        me = userRepository.save(User.create("a" + System.nanoTime() + "@test.com", "test123", "me"));
        other = userRepository.save(User.create("a" + System.nanoTime() + "@test.com", "test123", "other"));

        myReservation = createReservation(me);
        otherReservation = createReservation(other);
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("결제하면 예약이 확정되고 티켓이 판매 완료된다.")
    void 결제_성공() {
        //when
        PaymentCreateResponse response = paymentService.pay(me.getId(), new PaymentCreateRequest(myReservation.getId(), PaymentMethod.CARD));

        //then
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

        Reservation found = reservationRepository.findByIdWithTicket(myReservation.getId())
                .orElseThrow();
        assertThat(found.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(found.getTicket().getStatus()).isEqualTo(TicketStatus.SOLD);

        verify(ticketServerClient).confirmReservation(any(), any());
    }

    @Test
    @DisplayName("이미 결제한 금액은 다시 결제할 수 없다.")
    void 중복_결제() {
        //given
        paymentService.pay(me.getId(), new PaymentCreateRequest(myReservation.getId(), PaymentMethod.CARD));

        //when,then
        assertThatThrownBy(() -> paymentService.pay(me.getId(), new PaymentCreateRequest(myReservation.getId(), PaymentMethod.CARD)))
                .isInstanceOf(ReservationNotPendingException.class);
    }

    @Test
    @DisplayName("같은 예약에 동시에 결제 요청이 오면 1건만 성공한다.")
    void 동시_결제_테스트() throws InterruptedException {
        //given
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        //when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    paymentService.pay(me.getId(), new PaymentCreateRequest(myReservation.getId(), PaymentMethod.CARD));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        //then
        System.out.println("== 동시 결제 테스트 결과 ==");
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
        System.out.println("생성된 결제: " + paymentRepository.count());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(paymentRepository.count()).isEqualTo(1);

        // 티켓서버 확정은 한 번만 호출되어야 이중 결제가 아님
        verify(ticketServerClient, times(1)).confirmReservation(any(), any());
    }

    @Test
    @DisplayName("본인 결제를 조회하면 티켓 정보와 함께 1건 반환한다")
    void 결제_조회_성공() {
        //given
        PaymentCreateResponse created = paymentService.pay(me.getId(), new PaymentCreateRequest(myReservation.getId(), PaymentMethod.CASH));

        //when
        PaymentGetResponse result = paymentTransactionService.getOnePayment(me.getId(), created.getId());

        //then
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getTicket().getProgramName()).isEqualTo("테스트 공연");
    }

    @Test
    @DisplayName("본인 결제를 전체 조회하면 티켓 정보와 함께 전체 반환된다.")
    void 결제_전체_조회_성공() {
        Ticket ticket2 = ticketRepository.save(Ticket.builder()
                .externalTicketId(System.nanoTime())
                .performanceId(1L)
                .programName("테스트 공연2")
                .startAt(LocalDateTime.now().plusDays(30))
                .venue("테스트 공연장")
                .seatGrade("VIP")
                .price(TICKET_PRICE)
                .status(TicketStatus.RESERVED)
                .build());
        Ticket ticket3 = ticketRepository.save(Ticket.builder()
                .externalTicketId(System.nanoTime())
                .performanceId(1L)
                .programName("테스트 공연3")
                .startAt(LocalDateTime.now().plusDays(30))
                .venue("테스트 공연장")
                .seatGrade("VIP")
                .price(TICKET_PRICE)
                .status(TicketStatus.RESERVED)
                .build());

        Reservation reservation1 = reservationRepository.save(Reservation.create(me, ticket2, LocalDateTime.now().plusMinutes(10)));
        Reservation reservation2 = reservationRepository.save(Reservation.create(me, ticket3, LocalDateTime.now().plusMinutes(10)));

        paymentService.pay(me.getId(), new PaymentCreateRequest(myReservation.getId(), PaymentMethod.CASH));
        paymentService.pay(me.getId(), new PaymentCreateRequest(reservation1.getId(), PaymentMethod.CASH));
        paymentService.pay(me.getId(), new PaymentCreateRequest(reservation2.getId(), PaymentMethod.CASH));

        //when
        List<PaymentGetResponse> result = paymentTransactionService.getAllPayment(me.getId(), 0, 3);

        //then
        assertThat(result.size()).isEqualTo(3);
    }

    private Reservation createReservation(User user) {
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .externalTicketId(System.nanoTime())
                .performanceId(1L)
                .programName("테스트 공연")
                .startAt(LocalDateTime.now().plusDays(30))
                .venue("테스트 공연장")
                .seatGrade("VIP")
                .price(TICKET_PRICE)
                .status(TicketStatus.RESERVED)
                .build());

        return reservationRepository.save(
                Reservation.create(user, ticket, LocalDateTime.now().plusMinutes(10)));
    }
}