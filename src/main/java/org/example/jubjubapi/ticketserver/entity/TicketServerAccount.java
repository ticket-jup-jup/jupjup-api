package org.example.jubjubapi.ticketserver.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.global.entity.BaseEntity;
import org.example.jubjubapi.user.entity.User;

@Getter
@Entity
@Table(name = "ticket_server_account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketServerAccount extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private Long externalUserId;

    private TicketServerAccount(User user, Long externalUserId) {
        this.user = user;
        this.externalUserId = externalUserId;
    }


    public static TicketServerAccount link(User user, Long externalUserId) {
        return new TicketServerAccount(user, externalUserId);
    }

    public void relink(Long externalUserId) {
        this.externalUserId = externalUserId;
    }
}
