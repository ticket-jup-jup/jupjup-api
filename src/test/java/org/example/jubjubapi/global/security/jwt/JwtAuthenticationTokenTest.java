package org.example.jubjubapi.global.security.jwt;


import org.example.jubjubapi.user.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationTokenTest {

    private final JwtUserPrincipal principal = new JwtUserPrincipal(7L, "user@example.com", Role.USER);

    @Test
    @DisplayName("JwtUserPrincipal은 role에 맞는 권한(ROLE_USER) 하나를 가진다")
    void principal_hasSingleRoleAuthority() {
        // when
        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        // then
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }

    @Test
    @DisplayName("생성 직후 인증 완료 상태이며 principal, name, 권한이 올바르다")
    void constructor_createsAuthenticatedToken() {
        // when
        JwtAuthenticationToken token = new JwtAuthenticationToken(principal);

        // then
        assertTrue(token.isAuthenticated());
        assertSame(principal, token.getPrincipal());
        assertEquals("7", token.getName());
        assertNull(token.getCredentials());
        assertTrue(token.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("외부에서 setAuthenticated(true)를 호출하면 예외")
    void setAuthenticatedTrue_throws() {
        // given
        JwtAuthenticationToken token = new JwtAuthenticationToken(principal);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> token.setAuthenticated(true));
    }

    @Test
    @DisplayName("setAuthenticated(false)는 허용되며 인증 상태가 해제된다")
    void setAuthenticatedFalse_allowed() {
        // given
        JwtAuthenticationToken token = new JwtAuthenticationToken(principal);

        // when
        token.setAuthenticated(false);

        // then
        assertFalse(token.isAuthenticated());
    }
}