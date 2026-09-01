package org.example.jubjubapi.auth.service;

import org.example.jubjubapi.auth.dto.SigninRequest;
import org.example.jubjubapi.auth.dto.SigninResponse;
import org.example.jubjubapi.auth.dto.SignupRequest;
import org.example.jubjubapi.auth.exception.DuplicateEmailException;
import org.example.jubjubapi.auth.exception.InvalidCredentialsException;
import org.example.jubjubapi.global.security.jwt.JwtProvider;
import org.example.jubjubapi.user.entity.Role;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProvider jwtProvider;

    @InjectMocks AuthService authService;

    //회원가입
    @Test
    @DisplayName("회원가입 성공: 비밀번호를 해시해서 저장한다")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest("user@example.com", "password123", "홍길동");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);   // DB 가 채워줄 id 를 흉내
            return saved;
        });

        // when
        Long userId = authService.signup(request);

        // then
        assertEquals(1L, userId);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("user@example.com", saved.getEmail());
        assertEquals("hashed", saved.getPasswordHash());       // 평문이 아닌 해시가 저장됨
        assertEquals("홍길동", saved.getName());
        assertEquals(Role.USER, saved.getRole());
    }

    @Test
    @DisplayName("회원가입 실패: 이메일이 이미 존재하면 DuplicateEmailException, 저장하지 않는다")
    void signup_duplicateEmail() {
        // given
        SignupRequest request = new SignupRequest("user@example.com", "password123", "홍길동");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        // when & then
        assertThrows(DuplicateEmailException.class, () -> authService.signup(request));
        verify(userRepository, never()).save(any());
    }

    // ── 로그인 ────────────────────────────────────────────────

    @Test
    @DisplayName("로그인 성공: 토큰을 발급해서 반환한다")
    void signin_success() {
        // given
        User user = activeUser(1L, "user@example.com", "hashed");
        SigninRequest request = new SigninRequest("user@example.com", "password123");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtProvider.createToken(1L, "user@example.com", Role.USER)).thenReturn("jwt-token");

        // when
        SigninResponse response = authService.signin(request);

        // then
        assertEquals("jwt-token", response.accessToken());
    }

    @Test
    @DisplayName("로그인 실패: 존재하지 않는 이메일이면 InvalidCredentialsException")
    void signin_unknownEmail() {
        // given
        SigninRequest request = new SigninRequest("nobody@example.com", "password123");
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(InvalidCredentialsException.class, () -> authService.signin(request));
        verify(jwtProvider, never()).createToken(any(), any(), any());
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호가 틀리면 InvalidCredentialsException")
    void signin_wrongPassword() {
        // given
        User user = activeUser(1L, "user@example.com", "hashed");
        SigninRequest request = new SigninRequest("user@example.com", "wrong-password");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        // when & then
        assertThrows(InvalidCredentialsException.class, () -> authService.signin(request));
        verify(jwtProvider, never()).createToken(any(), any(), any());
    }

    @Test
    @DisplayName("로그인 실패: 탈퇴한 사용자는 비밀번호가 맞아도 InvalidCredentialsException")
    void signin_withdrawnUser() {
        // given
        User user = activeUser(1L, "user@example.com", "hashed");
        user.withdraw(LocalDateTime.now());   // status → DELETED
        SigninRequest request = new SigninRequest("user@example.com", "password123");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // when & then
        assertThrows(InvalidCredentialsException.class, () -> authService.signin(request));
        verify(passwordEncoder, never()).matches(any(), any());   // 비밀번호 비교 전에 차단
        verify(jwtProvider, never()).createToken(any(), any(), any());
    }

    // ── 헬퍼 ──────────────────────────────────────────────────

    /** id 가 세팅된 활성 사용자 생성 (엔티티에 id setter 가 없어서 리플렉션 사용) */
    private User activeUser(Long id, String email, String passwordHash) {
        User user = User.create(email, passwordHash, "홍길동");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}

