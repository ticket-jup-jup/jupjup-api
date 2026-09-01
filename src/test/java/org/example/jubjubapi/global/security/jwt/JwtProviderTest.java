package org.example.jubjubapi.global.security.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.example.jubjubapi.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtProviderTest {

    // 테스트 전용 키 (Base64, 48바이트). 운영 키와 무관.
    private static final String SECRET = "ixiDJ646uYZDZhs7R6FmJGdhfiOVmgecx32Y6rGv7IyDS8OW4g+7flGw1se7hObS";
    private static final String OTHER_SECRET = "u+AG/ub2Ukqq7VPP5mjA4bl/EbrzykokJvf7xnxGt+NWTgFS8ZTc5rugSFy35SOo";
    private static final long ONE_HOUR = 3_600_000L;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, ONE_HOUR);
    }

    @Test
    @DisplayName("발급한 토큰을 파싱하면 userId, email, role이 그대로 나온다")
    void createToken_and_getClaims_roundTrip() {
        // given
        Long userId = 1L;
        String email = "user@example.com";
        Role role = Role.USER;

        // when
        String token = jwtProvider.createToken(userId, email, role);
        Claims claims = jwtProvider.getClaims(token);

        // then
        assertEquals("1", claims.getSubject());
        assertEquals(email, claims.get(JwtProvider.EMAIL_CLAIM, String.class));
        assertEquals("USER", claims.get(JwtProvider.ROLE_CLAIM, String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("만료 시각은 발급 시각 + 설정한 만료 시간이다")
    void expiration_isIssuedAtPlusConfiguredMillis() {
        // given
        String token = jwtProvider.createToken(1L, "user@example.com", Role.USER);

        // when
        Claims claims = jwtProvider.getClaims(token);
        long diff = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        // then
        assertEquals(ONE_HOUR, diff);
    }

    @Test
    @DisplayName("만료된 토큰을 파싱하면 ExpiredJwtException")
    void getClaims_expiredToken_throwsExpiredJwtException() {
        // given: 만료 시간을 음수로 주면 발급 즉시 만료된 토큰이 만들어진다.
        JwtProvider expiredProvider = new JwtProvider(SECRET, -10_000L);
        String expiredToken = expiredProvider.createToken(1L, "user@example.com", Role.USER);

        // when & then
        assertThrows(ExpiredJwtException.class, () -> jwtProvider.getClaims(expiredToken));
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰을 파싱하면 JwtException")
    void getClaims_wrongSignature_throwsJwtException() {
        // given
        JwtProvider otherProvider = new JwtProvider(OTHER_SECRET, ONE_HOUR);
        String foreignToken = otherProvider.createToken(1L, "user@example.com", Role.USER);

        // when & then
        assertThrows(JwtException.class, () -> jwtProvider.getClaims(foreignToken));
    }

    @Test
    @DisplayName("JWT 형식이 아닌 문자열을 파싱하면 JwtException")
    void getClaims_malformed_throwsJwtException() {
        // given
        String malformed = "not.a.jwt";

        // when & then
        assertThrows(JwtException.class, () -> jwtProvider.getClaims(malformed));
    }

    @Test
    @DisplayName("null 또는 빈 문자열을 파싱하면 IllegalArgumentException")
    void getClaims_nullOrEmpty_throwsIllegalArgumentException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> jwtProvider.getClaims(null));
        assertThrows(IllegalArgumentException.class, () -> jwtProvider.getClaims(""));
    }
}