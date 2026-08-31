package org.example.jubjubapi.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.user.entity.Role;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 토큰이 없으면 그냥 통과. 허용 여부는 SecurityConfig 가 판단한다.
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        try {
            authenticate(token, request);
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException: 서명 불일치·만료·형식 오류
            // IllegalArgumentException: subject 가 숫자가 아님, role 값이 이상함, 필수 claim 누락
            sendUnauthorized(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        Claims claims = jwtProvider.getClaims(token);

        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get(JwtProvider.EMAIL_CLAIM, String.class);
        String roleName = claims.get(JwtProvider.ROLE_CLAIM, String.class);
        if (email == null || roleName == null) {
            throw new IllegalArgumentException("JWT에 필수 정보가 없습니다.");
        }
        Role role = Role.valueOf(roleName);

        JwtUserPrincipal principal = new JwtUserPrincipal(userId, email, role);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(principal);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("유효하지 않거나 만료된 JWT입니다.");
    }
}
