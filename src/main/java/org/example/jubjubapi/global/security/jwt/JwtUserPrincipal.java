package org.example.jubjubapi.global.security.jwt;

import org.example.jubjubapi.user.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.util.Collection;
import java.util.List;

public record JwtUserPrincipal(Long userId, String email, Role role) {

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.authority())); // "ROLE_USER"
    }
}

