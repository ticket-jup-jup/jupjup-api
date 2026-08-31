package org.example.jubjubapi.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 30)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(nullable = false)
    private long securityVersion;

    private LocalDateTime deletedAt;

    public User(String email, String passwordHash, String name) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = Role.USER;
        this.status = UserStatus.ACTIVE;
        this.securityVersion = 0L;
    }

    public static User create(
            String email,
            String passwordHash,
            String name
    ) {
        return new User(
                email,
                passwordHash,
                name
        );
    }

    public void changePassword(
            String encodedPassword
    ) {
        this.passwordHash = encodedPassword;
        this.securityVersion++;
    }

    public void withdraw(LocalDateTime now){
        this.status = UserStatus.DELETED;
        this.securityVersion++;
        this.deletedAt = now;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
