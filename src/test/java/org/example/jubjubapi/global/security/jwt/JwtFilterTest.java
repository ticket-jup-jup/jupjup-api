package org.example.jubjubapi.global.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.example.jubjubapi.user.entity.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JwtFilter 단위 테스트.
 * 실제 JwtProvider 를 사용하고, 요청/응답/필터체인은 spring-test 의 Mock 객체를 쓴다.
 *
 * 확인 포인트
 *   - chain.getRequest() != null  → 다음 필터로 넘어갔다
 *   - chain.getRequest() == null  → 필터에서 요청이 끊겼다 (401 응답)
 */
class JwtFilterTest {

    private static final String SECRET = "ixiDJ646uYZDZhs7R6FmJGdhfiOVmgecx32Y6rGv7IyDS8OW4g+7flGw1se7hObS";
    private static final String OTHER_SECRET = "u+AG/ub2Ukqq7VPP5mjA4bl/EbrzykokJvf7xnxGt+NWTgFS8ZTc5rugSFy35SOo";
    private static final long ONE_HOUR = 3_600_000L;

    private JwtProvider jwtProvider;
    private JwtFilter jwtFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, ONE_HOUR);
        jwtFilter = new JwtFilter(jwtProvider);
        request = new MockHttpServletRequest("GET", "/users/me");
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── 통과 케이스 ──────────────────────────────────────────

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 없이 다음 필터로 넘어간다")
    void noHeader_passesThroughWithoutAuthentication() throws Exception {
        // given: 헤더 없음 (setUp 상태 그대로)

        // when
        jwtFilter.doFilter(request, response, chain);

        // then
        assertNotNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    @DisplayName("Bearer 형식이 아닌 헤더는 무시하고 다음 필터로 넘어간다")
    void nonBearerHeader_passesThroughWithoutAuthentication() throws Exception {
        // given
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        // when
        jwtFilter.doFilter(request, response, chain);

        // then
        assertNotNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("유효한 토큰이면 SecurityContext에 인증 정보가 저장되고 다음 필터로 넘어간다")
    void validToken_setsAuthentication() throws Exception {
        // given
        String token = jwtProvider.createToken(42L, "user@example.com", Role.USER);
        request.addHeader("Authorization", "Bearer " + token);

        // when
        jwtFilter.doFilter(request, response, chain);

        // then
        assertNotNull(chain.getRequest());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication instanceof JwtAuthenticationToken);
        assertTrue(authentication.isAuthenticated());

        JwtUserPrincipal principal = (JwtUserPrincipal) authentication.getPrincipal();
        assertEquals(42L, principal.userId());
        assertEquals("user@example.com", principal.email());
        assertEquals(Role.USER, principal.role());

        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
    }

    // ── 거부 케이스 (401) ─────────────────────────────────────

    @Test
    @DisplayName("만료된 토큰이면 401을 응답하고 다음 필터로 넘어가지 않는다")
    void expiredToken_returns401() throws Exception {
        // given
        String expiredToken = new JwtProvider(SECRET, -10_000L)
                .createToken(1L, "user@example.com", Role.USER);
        request.addHeader("Authorization", "Bearer " + expiredToken);

        // when
        jwtFilter.doFilter(request, response, chain);

        // then
        assertRejected();
    }

    @Test
    @DisplayName("서명이 다른 토큰이면 401")
    void wrongSignature_returns401() throws Exception {
        // given
        String foreignToken = new JwtProvider(OTHER_SECRET, ONE_HOUR)
                .createToken(1L, "user@example.com", Role.USER);
        request.addHeader("Authorization", "Bearer " + foreignToken);

        // when
        jwtFilter.doFilter(request, response, chain);

        // then
        assertRejected();
    }

    @Test
    @DisplayName("JWT 형식이 아닌 토큰이면 401")
    void malformedToken_returns401() throws Exception {
        // given
        request.addHeader("Authorization", "Bearer not.a.jwt");

        // when
        jwtFilter.doFilter(request, response, chain);

        // then
        assertRejected();
    }

    @Test
    @DisplayName("email claim이 없는 토큰이면 401")
    void missingEmailClaim_returns401() throws Exception {
        // given: email 없이 직접 만든 토큰
        String token = Jwts.builder()
                .subject("1")
                .claim(JwtProvider.ROLE_CLAIM, "USER")
                .expiration(new Date(System.currentTimeMillis() + ONE_HOUR))
                .signWith(secretKey())
                .compact();
        request.addHeader("Authorization", "Bearer " + token);

        // when
        jwtFilter.doFilter(request, response, chain);

        // then
        assertRejected();
    }

    @Test
    @DisplayName("subject(userId)가 숫자가 아니면 401")
    void nonNumericSubject_returns401() throws Exception {
        // given
        String token = Jwts.builder()
                .subject("not-a-number")
                .claim(JwtProvider.EMAIL_CLAIM, "user@example.com")
                .claim(JwtProvider.ROLE_CLAIM, "USER")
                .expiration(new Date(System.currentTimeMillis() + ONE_HOUR))
                .signWith(secretKey())
                .compact();
        request.addHeader("Authorization", "Bearer " + token);

        // when
        jwtFilter.doFilter(request, response, chain);

        // then
        assertRejected();
    }

    @Test
    @DisplayName("role 값이 유효하지 않으면 401")
    void invalidRole_returns401() throws Exception {
        // given
        String token = Jwts.builder()
                .subject("1")
                .claim(JwtProvider.EMAIL_CLAIM, "user@example.com")
                .claim(JwtProvider.ROLE_CLAIM, "SUPER_ADMIN")
                .expiration(new Date(System.currentTimeMillis() + ONE_HOUR))
                .signWith(secretKey())
                .compact();
        request.addHeader("Authorization", "Bearer " + token);

        // when
        jwtFilter.doFilter(request, response, chain);

        // then
        assertRejected();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────

    /** 401 응답 + 체인 중단 + SecurityContext 비어 있음 을 한 번에 검증 */
    private void assertRejected() throws Exception {
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(response.getContentAsString().contains("JWT"));
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }
}