package org.example.jubjubapi.ticket;

import org.example.jubjubapi.global.exception.ServiceException;
import org.example.jubjubapi.ticket.entity.*;
import org.example.jubjubapi.ticket.repository.*;
import org.example.jubjubapi.ticket.service.TicketService;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TicketServiceTest {
    private TicketRepository tickets;
    private TicketWatchRepository watches;
    private UserRepository users;
    private TicketService service;
    private User user;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        tickets = mock(TicketRepository.class);
        watches = mock(TicketWatchRepository.class);

        users = mock(UserRepository.class);
        service = new TicketService(tickets, watches, users);
        user = User.create("test@example.com", "encoded-password", "사용자");
        ReflectionTestUtils.setField(user, "id", 1L);
        ticket = Ticket.builder().externalTicketId(100L).performanceId(10L)
                .programName("공연").startAt(LocalDateTime.of(2026, 10, 1, 19, 0))
                .venue("공연장").price(new BigDecimal("50000.00"))
                .status(TicketStatus.SOLD).build();
        ReflectionTestUtils.setField(ticket, "id", 2L);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(tickets.findByIdForUpdate(2L)).thenReturn(Optional.of(ticket));
    }

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
}