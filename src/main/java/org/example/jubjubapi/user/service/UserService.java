package org.example.jubjubapi.user.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.user.dto.ChangePasswordRequest;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.exception.PasswordMismatchException;
import org.example.jubjubapi.user.exception.SamePasswordException;
import org.example.jubjubapi.user.exception.UserNotFoundException;
import org.example.jubjubapi.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findActiveUser(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new PasswordMismatchException();
        }
        if(passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new SamePasswordException();
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = findActiveUser(userId);
        user.withdraw(LocalDateTime.now());
    }

    private User findActiveUser(Long userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(UserNotFoundException::new);
    }
}
