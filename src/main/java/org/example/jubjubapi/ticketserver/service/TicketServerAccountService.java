package org.example.jubjubapi.ticketserver.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.ticket.client.TicketServerClient;
import org.example.jubjubapi.ticketserver.dto.LinkTicketServerAccountRequest;
import org.example.jubjubapi.ticketserver.dto.TicketServerAccountResponse;
import org.example.jubjubapi.ticketserver.entity.TicketServerAccount;
import org.example.jubjubapi.ticketserver.exception.TicketServerAccountAlreadyLinkedException;
import org.example.jubjubapi.ticketserver.exception.TicketServerAuthFailedException;
import org.example.jubjubapi.ticketserver.repository.TicketServerAccountRepository;
import org.example.jubjubapi.user.entity.User;
import org.example.jubjubapi.user.exception.UserNotFoundException;
import org.example.jubjubapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketServerAccountService {

    private final UserRepository userRepository;
    private final TicketServerAccountRepository ticketServerAccountRepository;
    private final TicketServerClient ticketServerClient;

    @Transactional
    public TicketServerAccountResponse link(Long userId, LinkTicketServerAccountRequest request) {
        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(UserNotFoundException::new);

        TicketServerClient.TicketServerUser ticketUser = ticketServerClient
                .verify(request.ticketServerEmail(), request.ticketServerPassword())
                .orElseThrow(TicketServerAuthFailedException::new);
        Long externalUserId = ticketUser.userId();

        Optional<TicketServerAccount> existing = ticketServerAccountRepository.findByUserId(userId);

        boolean sameAsCurrent = existing.isPresent()
                && existing.get().getExternalUserId().equals(externalUserId);
        if (!sameAsCurrent && ticketServerAccountRepository.existsByExternalUserId(externalUserId)) {
            throw new TicketServerAccountAlreadyLinkedException();
        }

        TicketServerAccount account;
        if (existing.isPresent()) {
            account = existing.get();
            account.relink(externalUserId);
        } else {
            account = ticketServerAccountRepository.save(TicketServerAccount.link(user, externalUserId));
        }

        return TicketServerAccountResponse.from(account);
    }
}