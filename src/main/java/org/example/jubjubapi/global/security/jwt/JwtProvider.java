package org.example.jubjubapi.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.jubjubapi.global.security.exception.InvalidTokenException;
import org.example.jubjubapi.user.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProvider {

    public static final String TOKEN_TYPE_CLAIM = "type";
    public static final String SECURITY_VERSION_CLAIM = "ver";
    public static final String EMAIL_CLAIM = "email";
    public static final String ROLE_CLAIM = "role";
    public static final String ACCESS_TOKEN_TYPE = "ACCESS";
    public static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final SecretKey secretKey;
    private final JwtParser jwtParser;
    private final String issuer;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(@Value("${jwt.secret}") String encodedSecret,
                       @Value("${jwt.issuer}") String issuer,
                       @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
                       @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {

        // Base64 형식·키 길이(256bit 이상) 검증은 jjwt가 수행 (DecodingException / WeakKeyException)
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
        this.issuer = issuer;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .clockSkewSeconds(30)
                .build();
    }

    // ── 생성 ────────────────────────────────────────────

    public String createAccessToken(Long userId, String email, Role role, long securityVersion) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpiration)))
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .claim(EMAIL_CLAIM, email)
                .claim(ROLE_CLAIM, role.name())
                .claim(SECURITY_VERSION_CLAIM, securityVersion)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId, long securityVersion) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshTokenExpiration)))
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .claim(SECURITY_VERSION_CLAIM, securityVersion)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // ── 파싱 ────────────────────────────────────────────

    public ParsedAccessToken parseAccessToken(String token) {
        Claims claims = parse(token, ACCESS_TOKEN_TYPE);
        return new ParsedAccessToken(
                extractUserId(claims),
                claims.get(EMAIL_CLAIM, String.class),
                extractRole(claims),
                extractSecurityVersion(claims)
        );
    }

    public ParsedRefreshToken parseRefreshToken(String token) {
        Claims claims = parse(token, REFRESH_TOKEN_TYPE);
        return new ParsedRefreshToken(extractUserId(claims), extractSecurityVersion(claims));
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }

    // ── 내부 ────────────────────────────────────────────

    private Claims parse(String token, String expectedType) {
        Claims claims;
        try {
            claims = jwtParser.parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 불일치, 만료, issuer 불일치, 형식 오류, null/빈 문자열 전부 여기로
            throw new InvalidTokenException("유효하지 않은 토큰입니다.", e);
        }
        if (!expectedType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new InvalidTokenException("토큰 종류가 올바르지 않습니다.");
        }
        return claims;
    }

    private Long extractUserId(Claims claims) {
        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new InvalidTokenException("JWT 사용자 ID가 올바르지 않습니다.", e);
        }
    }

    private Role extractRole(Claims claims) {
        try {
            return Role.valueOf(claims.get(ROLE_CLAIM, String.class));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidTokenException("JWT role이 올바르지 않습니다.", e);
        }
    }

    private long extractSecurityVersion(Claims claims) {
        if (!(claims.get(SECURITY_VERSION_CLAIM) instanceof Number number)) {
            throw new InvalidTokenException("JWT securityVersion이 올바르지 않습니다.");
        }
        return number.longValue();
    }

    public record ParsedAccessToken(Long userId, String email, Role role, long securityVersion) { }

    public record ParsedRefreshToken(Long userId, long securityVersion) { }
}