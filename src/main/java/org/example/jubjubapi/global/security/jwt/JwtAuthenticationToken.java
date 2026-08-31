package org.example.jubjubapi.global.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final JwtUserPrincipal principal;

    public JwtAuthenticationToken(JwtUserPrincipal principal) {
        super(principal.getAuthorities());
        this.principal = principal;
        super.setAuthenticated(true);
    }

    // @AuthenticationPrincipal 은 이 메서드의 반환값을 컨트롤러 파라미터에 주입한다.
    @Override
    public JwtUserPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public String getName() {
        return String.valueOf(principal.userId());
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException("인증 완료 상태는 생성자로만 설정할 수 있습니다.");
        }
        super.setAuthenticated(false);
    }
}

