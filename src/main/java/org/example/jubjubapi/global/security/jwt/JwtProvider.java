package org.example.jubjubapi.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.jubjubapi.user.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    public static final String EMAIL_CLAIM = "email";
    public static final String ROLE_CLAIM = "role";

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtProvider(@Value("${jwt.secret}") String encodedSecret,
                       @Value("${jwt.expiration}") long expirationMillis) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
        this.expirationMillis = expirationMillis;
    }

    public String createToken(Long userId, String email, Role role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(EMAIL_CLAIM, email)
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(secretKey)
                .compact();
    }

    /** 서명·만료 검증 후 payload 반환. 실패하면 JwtException 을 던진다. */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}