package org.example.jubjubapi.user.service;

import org.example.jubjubapi.user.dto.ChangePasswordRequest;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.entity.UserStatus;
import org.example.jubjubapi.user.exception.PasswordMismatchException;
import org.example.jubjubapi.user.exception.SamePasswordException;
import org.example.jubjubapi.user.exception.UserNotFoundException;
import org.example.jubjubapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    // ── 비밀번호 변경 ──────────────────────────────────────────

    @Test
    @DisplayName("비밀번호 변경 성공: 새 해시로 교체되고 securityVersion이 1 증가한다")
    void changePassword_success() {
        // given
        User user = activeUser(1L, "old-hash");
        ChangePasswordRequest request = new ChangePasswordRequest("old-password", "new-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);   // 현재 비밀번호 일치
        when(passwordEncoder.matches("new-password", "old-hash")).thenReturn(false);  // 새 비밀번호는 현재와 다름
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        // when
        userService.changePassword(1L, request);

        // then
        assertEquals("new-hash", user.getPasswordHash());
        assertEquals(1L, user.getSecurityVersion());   // 0 → 1
    }

    @Test
    @DisplayName("비밀번호 변경 실패: 현재 비밀번호가 틀리면 PasswordMismatchException")
    void changePassword_wrongCurrentPassword() {
        // given
        User user = activeUser(1L, "old-hash");
        ChangePasswordRequest request = new ChangePasswordRequest("wrong-password", "new-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        // when & then
        assertThrows(PasswordMismatchException.class, () -> userService.changePassword(1L, request));
        assertEquals("old-hash", user.getPasswordHash());   // 변경되지 않음
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("비밀번호 변경 실패: 새 비밀번호가 현재와 같으면 SamePasswordException")
    void changePassword_samePassword() {
        // given
        User user = activeUser(1L, "old-hash");
        ChangePasswordRequest request = new ChangePasswordRequest("old-password", "old-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);

        // when & then
        assertThrows(SamePasswordException.class, () -> userService.changePassword(1L, request));
        assertEquals("old-hash", user.getPasswordHash());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("비밀번호 변경 실패: 사용자가 없으면 UserNotFoundException")
    void changePassword_userNotFound() {
        // given
        ChangePasswordRequest request = new ChangePasswordRequest("old-password", "new-password");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.changePassword(99L, request));
    }

    @Test
    @DisplayName("비밀번호 변경 실패: 이미 탈퇴한 사용자면 UserNotFoundException")
    void changePassword_withdrawnUser() {
        // given
        User user = activeUser(1L, "old-hash");
        user.withdraw(LocalDateTime.now());
        ChangePasswordRequest request = new ChangePasswordRequest("old-password", "new-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.changePassword(1L, request));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    // ── 회원탈퇴 ──────────────────────────────────────────────

    @Test
    @DisplayName("회원탈퇴 성공: status가 DELETED가 되고 deletedAt이 기록되며 securityVersion이 증가한다")
    void withdraw_success() {
        // given
        User user = activeUser(1L, "hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        userService.withdraw(1L);

        // then
        assertEquals(UserStatus.DELETED, user.getStatus());
        assertFalse(user.isActive());
        assertNotNull(user.getDeletedAt());
        assertEquals(1L, user.getSecurityVersion());
        verify(userRepository, never()).delete(any());   // soft delete: 실제 삭제는 호출되지 않음
    }

    @Test
    @DisplayName("회원탈퇴 실패: 사용자가 없으면 UserNotFoundException")
    void withdraw_userNotFound() {
        // given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.withdraw(99L));
    }

    @Test
    @DisplayName("회원탈퇴 실패: 이미 탈퇴한 사용자면 UserNotFoundException")
    void withdraw_alreadyWithdrawn() {
        // given
        User user = activeUser(1L, "hash");
        user.withdraw(LocalDateTime.now());
        LocalDateTime firstDeletedAt = user.getDeletedAt();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.withdraw(1L));
        assertEquals(firstDeletedAt, user.getDeletedAt());   // 탈퇴 일시가 덮어써지지 않음
    }

    // ── 헬퍼 ──────────────────────────────────────────────────

    private User activeUser(Long id, String passwordHash) {
        User user = User.create("user@example.com", passwordHash, "홍길동");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}