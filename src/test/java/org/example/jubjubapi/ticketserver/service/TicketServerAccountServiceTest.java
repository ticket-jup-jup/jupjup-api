package org.example.jubjubapi.ticketserver.service;

import org.example.jubjubapi.ticket.client.TicketServerClient;
import org.example.jubjubapi.ticketserver.dto.LinkTicketServerAccountRequest;
import org.example.jubjubapi.ticketserver.dto.TicketServerAccountResponse;
import org.example.jubjubapi.ticketserver.entity.TicketServerAccount;
import org.example.jubjubapi.ticketserver.exception.TicketServerAccountAlreadyLinkedException;
import org.example.jubjubapi.ticketserver.exception.TicketServerAuthFailedException;
import org.example.jubjubapi.ticketserver.repository.TicketServerAccountRepository;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.exception.UserNotFoundException;
import org.example.jubjubapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServerAccountServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    TicketServerAccountRepository ticketServerAccountRepository;
    @Mock
    TicketServerClient ticketServerClient;

    @InjectMocks
    TicketServerAccountService service;

    private static final String EMAIL = "ticket@example.com";
    private static final String PASSWORD = "ticket-pw";
    private final LinkTicketServerAccountRequest request = new LinkTicketServerAccountRequest(EMAIL, PASSWORD);

    @Test
    @DisplayName("연동 성공: 티켓 서버 인증 통과 시 사용자 id 를 새 계정으로 저장한다")
    void link_createsNewAccount() {
        // given
        User user = activeUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketServerClient.verify(EMAIL, PASSWORD))
                .thenReturn(Optional.of(new TicketServerClient.TicketServerUser(7L, EMAIL, "홍길동")));
        when(ticketServerAccountRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(ticketServerAccountRepository.existsByExternalUserId(7L)).thenReturn(false);
        when(ticketServerAccountRepository.save(any(TicketServerAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        TicketServerAccountResponse response = service.link(1L, request);

        // then
        assertEquals(7L, response.externalUserId());
        ArgumentCaptor<TicketServerAccount> captor = ArgumentCaptor.forClass(TicketServerAccount.class);
        verify(ticketServerAccountRepository).save(captor.capture());
        assertSame(user, captor.getValue().getUser());
        assertEquals(7L, captor.getValue().getExternalUserId());
    }

    @Test
    @DisplayName("연동 성공: 이미 연동된 사용자가 다른 계정으로 바꾸면 기존 row 를 덮어쓴다 (save 호출 없음)")
    void link_relinksExistingAccount() {
        // given
        User user = activeUser(1L);
        TicketServerAccount existing = TicketServerAccount.link(user, 7L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketServerClient.verify(EMAIL, PASSWORD))
                .thenReturn(Optional.of(new TicketServerClient.TicketServerUser(9L, EMAIL, "홍길동")));
        when(ticketServerAccountRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(ticketServerAccountRepository.existsByExternalUserId(9L)).thenReturn(false);

        // when
        TicketServerAccountResponse response = service.link(1L, request);

        // then
        assertEquals(9L, response.externalUserId());
        assertEquals(9L, existing.getExternalUserId());              // 기존 엔티티가 바뀜
        verify(ticketServerAccountRepository, never()).save(any());   // 변경 감지에 맡김
    }

    @Test
    @DisplayName("연동 성공: 같은 계정으로 다시 연동하면 중복 검사 없이 통과한다")
    void link_sameAccountAgain() {
        // given
        User user = activeUser(1L);
        TicketServerAccount existing = TicketServerAccount.link(user, 7L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketServerClient.verify(EMAIL, PASSWORD))
                .thenReturn(Optional.of(new TicketServerClient.TicketServerUser(7L, EMAIL, "홍길동")));
        when(ticketServerAccountRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        // when
        TicketServerAccountResponse response = service.link(1L, request);

        // then
        assertEquals(7L, response.externalUserId());
        verify(ticketServerAccountRepository, never()).existsByExternalUserId(any());
    }

    @Test
    @DisplayName("연동 실패: 티켓 서버 인증 실패면 TicketServerAuthFailedException, 저장하지 않는다")
    void link_authFailed() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser(1L)));
        when(ticketServerClient.verify(EMAIL, PASSWORD)).thenReturn(Optional.empty());

        // when & then
        assertThrows(TicketServerAuthFailedException.class, () -> service.link(1L, request));
        verify(ticketServerAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("연동 실패: 다른 줍줍 사용자가 이미 연동한 계정이면 TicketServerAccountAlreadyLinkedException")
    void link_alreadyLinkedByOtherUser() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser(1L)));
        when(ticketServerClient.verify(EMAIL, PASSWORD))
                .thenReturn(Optional.of(new TicketServerClient.TicketServerUser(7L, EMAIL, "홍길동")));
        when(ticketServerAccountRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(ticketServerAccountRepository.existsByExternalUserId(7L)).thenReturn(true);

        // when & then
        assertThrows(TicketServerAccountAlreadyLinkedException.class, () -> service.link(1L, request));
        verify(ticketServerAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("연동 실패: 탈퇴한 사용자면 UserNotFoundException, 티켓 서버를 호출하지 않는다")
    void link_withdrawnUser() {
        // given
        User user = activeUser(1L);
        user.withdraw(LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        assertThrows(UserNotFoundException.class, () -> service.link(1L, request));
        verify(ticketServerClient, never()).verify(any(), any());
    }

    // ── 헬퍼 ──────────────────────────────────────────────────

    private User activeUser(Long id) {
        User user = User.create("user@example.com", "hash", "홍길동");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}