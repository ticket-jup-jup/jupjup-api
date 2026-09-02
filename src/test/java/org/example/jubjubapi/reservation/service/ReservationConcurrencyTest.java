package org.example.jubjubapi.reservation.service;

import org.example.jubjubapi.reservation.dto.request.ReservationCreateRequest;
import org.example.jubjubapi.reservation.repository.ReservationRepository;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.repository.TicketRepository;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("예약 동시성 테스트")
class ReservationConcurrencyTest {

    private static final int THREAD_COUNT = 10;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    private Ticket ticket;
    private List<Long> userIdList;
    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();

        // 예약 가능한 티켓 1장
        ticket = ticketRepository.save(Ticket.builder()
                .externalTicketId(System.currentTimeMillis())
                .performanceId(1L)
                .programName("동시성 테스트 공연")
                .startAt(LocalDateTime.now().plusDays(30))
                .venue("테스트 공연장")
                .seatGrade("VIP")
                .price(new BigDecimal("100000.00"))
                .status(TicketStatus.AVAILABLE)
                .build()
        );

        // 서로 다른 사용자 10명 생성
        userIdList = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            User user = userRepository.save(User.create("concurrency" + System.nanoTime() + "@test.com", "encoded", "테스터" + i));
            userIdList.add(user.getId());
        }
    }

    @Test
    @DisplayName("10명이 동시에 같은 티켓을 예약하면 1건만 성공해야 한다")
    void 동시_예약_테스트() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when (10개 스레드가 동시에 예약 시도)
        for (Long userId : userIdList) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    reservationService.reserve(userId, new ReservationCreateRequest(ticket.getId()));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("실패 사유: " + e.getClass().getSimpleName());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        long reservationCount = reservationRepository.count();
        Ticket foundTicket = ticketRepository.findById(ticket.getId()).orElseThrow();

        System.out.println("== 동시성 테스트 결과 ==");
        System.out.println("요청 수: " + THREAD_COUNT);
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
        System.out.println("생성된 예약: " + reservationCount);

        assertThat(reservationCount).isEqualTo(1);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(THREAD_COUNT - 1);
        assertThat(foundTicket.getStatus()).isEqualTo(TicketStatus.RESERVED);
    }
}