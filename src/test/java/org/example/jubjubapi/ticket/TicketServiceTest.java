package org.example.jubjubapi.ticket;

import org.example.jubjubapi.global.exception.ServiceException;
import org.example.jubjubapi.ticket.dto.TicketResponse;
import org.example.jubjubapi.ticket.entity.*;
import org.example.jubjubapi.ticket.exception.TicketErrorCode;
import org.example.jubjubapi.ticket.exception.TicketException;
//import org.example.jubjubapi.ticket.performance.repository.PerformanceWatchRepository;
import org.example.jubjubapi.ticket.repository.*;
import org.example.jubjubapi.ticket.service.TicketService;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TicketServiceTest {
    private TicketRepository tickets;
    //private PerformanceWatchRepository watches;
    private UserRepository users;
    private TicketService service;
    private User user;
    private Ticket ticket;

    private static final Long TICKET_ID = 2L;
    private static final Long PERFORMANCE_ID = 10L;

    @BeforeEach
    void setUp() {
        tickets = mock(TicketRepository.class);
        //watches = mock(PerformanceWatchRepository.class);

        users = mock(UserRepository.class);
        service = new TicketService(tickets, users);
        user = User.create("test@example.com", "encoded-password", "사용자");
        ReflectionTestUtils.setField(user, "id", 1L);
        ticket = Ticket.builder().externalTicketId(100L).performanceId(10L)
                .price(new BigDecimal("50000.00"))
                .status(TicketStatus.AVAILABLE).build();
        ReflectionTestUtils.setField(ticket, "id", 2L);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(tickets.findByIdForUpdate(2L)).thenReturn(Optional.of(ticket));



    }
    @Test
    @DisplayName("조회 조건의 회차 ID가 0 이하면 조회하지 않는다")
    void getTickets_invalidPerformanceId() {
        expectError(
                TicketErrorCode.INVALID_ID,
                () -> service.getTickets(
                        0L,
                        null,
                        0,
                        20
                )
        );

        verify(tickets, never())
                .search(any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("티켓을 ID로 단건 조회한다")
    void getTicket_success() {
        when(tickets.findById(TICKET_ID))
                .thenReturn(Optional.of(ticket));

        TicketResponse response =
                service.getTicket(TICKET_ID);

        assertEquals(TICKET_ID, response.getId());
        assertEquals(100L, response.getExternalTicketId());
        assertEquals(
                TicketStatus.AVAILABLE,
                response.getStatus()
        );
    }
    @Test
    @DisplayName("티켓 ID가 0 이하면 저장소를 조회하지 않는다")
    void getTicket_invalidId() {
        expectError(
                TicketErrorCode.INVALID_ID,
                () -> service.getTicket(0L)
        );

        verify(tickets, never())
                .findById(anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 티켓은 삭제할 수 없다")
    void deleteTicket_notFound() {
        when(tickets.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        expectError(
                TicketErrorCode.TICKET_NOT_FOUND,
                () -> service.deleteTicket(99L)
        );

        verify(tickets, never())
                .delete(any(Ticket.class));
    }

    @Test
    @DisplayName("예약이 참조하는 티켓은 삭제할 수 없다")
    void deleteTicket_reservationReferenceExists() {
        when(tickets.findByIdForUpdate(TICKET_ID))
                .thenReturn(Optional.of(ticket));

        when(tickets.countReservationReferences(TICKET_ID))
                .thenReturn(1L);

        expectError(
                TicketErrorCode.TICKET_IN_USE,
                () -> service.deleteTicket(TICKET_ID)
        );

        verify(tickets, never())
                .countRestrictiveReservationForeignKeys();

        verify(tickets, never())
                .delete(any(Ticket.class));
    }


    /*


    @Test
    @DisplayName("취소표 알림 구독 생성 성공")
    void createsWatchForAuthenticatedUser() {
        when(watches.findByUser_IdAndTicket_Id(1L, 2L)).thenReturn(Optional.empty());
        when(watches.saveAndFlush(any(TicketWatch.class))).thenAnswer(invocation -> {
            TicketWatch saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 3L);
            assertSame(user, saved.getUser());
            assertSame(ticket, saved.getTicket());
            return saved;
        });
        var response = service.createWatch(1L, 2L);
        assertEquals(3L, response.getId());
        assertEquals(2L, response.getTicketId());
        assertEquals(TicketWatchStatus.ACTIVE, response.getStatus());
    }

    @Test
    @DisplayName("활성 구독 중복 생성 실패")
    void rejectsDuplicateActiveWatch() {
        TicketWatch watch = TicketWatch.create(user, ticket);
        when(watches.findByUser_IdAndTicket_Id(1L, 2L)).thenReturn(Optional.of(watch));
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.createWatch(1L, 2L));
        assertEquals("WATCH_ALREADY_EXISTS", error.getCode());
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(watches, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("비활성 구독 재활성화 성공")
    void reusesWatchIdWhenResubscribing() {
        TicketWatch watch = TicketWatch.create(user, ticket);
        ReflectionTestUtils.setField(watch, "id", 3L);
        watch.deactivate();
        when(watches.findByUser_IdAndTicket_Id(1L, 2L)).thenReturn(Optional.of(watch));
        when(watches.saveAndFlush(watch)).thenReturn(watch);
        assertEquals(3L, service.createWatch(1L, 2L).getId());
        assertTrue(watch.isActive());
        verify(watches).saveAndFlush(watch);
    }

    @Test
    @DisplayName("다른 사용자 구독 해제 실패")
    void cannotDeactivateAnotherUsersWatch() {
        when(watches.findByIdAndUser_Id(30L, 1L)).thenReturn(Optional.empty());
        var error = assertThrows(ServiceException.class, () -> service.deactivateWatch(1L, 30L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        verify(watches).findByIdAndUser_Id(30L, 1L);
    }

    @Test
    @DisplayName("구독 해제 멱등성 검증")
    void unsubscribeIsIdempotentAndKeepsRow() {
        TicketWatch watch = TicketWatch.create(user, ticket);
        when(watches.findByIdAndUser_Id(3L, 1L)).thenReturn(Optional.of(watch));
        service.deactivateWatch(1L, 3L);
        service.deactivateWatch(1L, 3L);
        assertFalse(watch.isActive());
        verify(watches, never()).delete(any());
    }

    @Test
    @DisplayName("비활성 사용자 구독 실패")
    void unavailableUserCannotSubscribe() {
        user.withdraw(LocalDateTime.now());
        var error = assertThrows(ServiceException.class, () -> service.createWatch(1L, 2L));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatus());
        verify(tickets, never()).findByIdForUpdate(anyLong());
    }

    @Test
    @DisplayName("구독 참조 티켓 삭제 실패")
    void anyWatchReferenceBlocksPhysicalDeletion() {
        when(watches.existsByTicket_Id(2L)).thenReturn(true);
        assertDeletionConflict("TICKET_IN_USE");
    }

     */

    @Test
    @DisplayName("티켓 목록을 조건과 페이징에 맞게 조회한다")
    void getTickets_success() {
        when(tickets.search(
                eq(PERFORMANCE_ID),
                eq(TicketStatus.AVAILABLE),
                any(Pageable.class)
        )).thenReturn(List.of(ticket));

        List<TicketResponse> responses = service.getTickets(
                PERFORMANCE_ID,
                TicketStatus.AVAILABLE,
                1,
                20
        );

        assertEquals(1, responses.size());

        TicketResponse response = responses.get(0);

        assertEquals(TICKET_ID, response.getId());
        assertEquals(100L, response.getExternalTicketId());
        assertEquals(
                new BigDecimal("50000.00"),
                response.getPrice()
        );
        assertEquals(
                TicketStatus.AVAILABLE,
                response.getStatus()
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(tickets).search(
                eq(PERFORMANCE_ID),
                eq(TicketStatus.AVAILABLE),
                captor.capture()
        );

        Pageable pageable = captor.getValue();

        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort()
                        .getOrderFor("id")
                        .getDirection()
        );
    }

    @Test
    @DisplayName("예약 참조 티켓 삭제 실패")
    void reservationReferenceBlocksPhysicalDeletion() {
        when(tickets.countReservationReferences(2L)).thenReturn(1L);
        assertDeletionConflict("TICKET_IN_USE");
    }

    @Test
    @DisplayName("외래키 미설정 티켓 삭제 실패")
    void missingDatabaseForeignKeyBlocksPhysicalDeletion() {
        when(tickets.countRestrictiveReservationForeignKeys()).thenReturn(0L);
        assertDeletionConflict("TICKET_DELETE_NOT_READY");
    }

    @Test
    @DisplayName("미참조 티켓 삭제 성공")
    void deletesUnreferencedTicketWhenDatabaseProtectionExists() {
        when(tickets.countRestrictiveReservationForeignKeys()).thenReturn(1L);
        service.deleteTicket(2L);
        verify(tickets).delete(ticket);
        verify(tickets).flush();
    }

    @Test
    @DisplayName("티켓 삭제 중 외래키 충돌")
    void concurrentReferenceConflictReturns409() {
        when(tickets.countRestrictiveReservationForeignKeys()).thenReturn(1L);
        doThrow(new DataIntegrityViolationException("foreign key")).when(tickets).flush();
        var error = assertThrows(ServiceException.class, () -> service.deleteTicket(2L));
        assertEquals("TICKET_IN_USE", error.getCode());
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    @DisplayName("페이지 크기 초과 요청 실패")
    void oversizedPageIsRejectedBeforeQuery() {
        var error = assertThrows(ServiceException.class,
                () -> service.getTickets(null, null, 0, 101));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        verify(tickets, never()).search(any(), any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 티켓 조회 실패")
    void missingTicketReturns404() {
        when(tickets.findById(99L)).thenReturn(Optional.empty());
        var error = assertThrows(ServiceException.class, () -> service.getTicket(99L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    private void assertDeletionConflict(String code) {
        var error = assertThrows(ServiceException.class, () -> service.deleteTicket(2L));
        assertEquals(code, error.getCode());
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(tickets, never()).delete(any());
    }

    //에러코드 헬퍼클래스
    private void expectError(
            TicketErrorCode expected,
            Executable executable
    ) {
        TicketException error =
                assertThrows(TicketException.class, executable);

        assertEquals(expected.getCode(), error.getCode());
        assertEquals(expected.getStatus(), error.getStatus());
        assertEquals(expected.getMessage(), error.getMessage());
    }
}