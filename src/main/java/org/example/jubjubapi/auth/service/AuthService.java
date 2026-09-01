package org.example.jubjubapi.auth.service;

import com.sun.jdi.request.DuplicateRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.auth.dto.SigninRequest;
import org.example.jubjubapi.auth.dto.SigninResponse;
import org.example.jubjubapi.auth.dto.SignupRequest;
import org.example.jubjubapi.auth.exception.DuplicateEmailException;
import org.example.jubjubapi.auth.exception.InvalidCredentialsException;
import org.example.jubjubapi.global.security.jwt.JwtProvider;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public Long signup(SignupRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }
        String passwordHash = passwordEncoder.encode(request.passowrd());
        User user = User.create(
                request.email(),
                passwordHash,
                request.name());
        return userRepository.save(user).getId();
    }

    @Transactional(readOnly = true)
    public SigninResponse signin(@Valid SigninRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException());
        if(!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtProvider
                .createToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole());
        return new SigninResponse(accessToken);
    }
}
