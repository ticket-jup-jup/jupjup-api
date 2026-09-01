package org.example.jubjubapi.ticket.client;

import lombok.extern.slf4j.Slf4j;
import org.example.jubjubapi.ticketserver.exception.TicketServerUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TicketServerClient {

    private final RestClient restClient;

    public TicketServerClient(
            RestClient.Builder restClientBuilder,
            @Value("${ticket-server.url}") String url,
            @Value("${ticket-server.api-key}") String apiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(url)
                .defaultHeader("X-API-KEY", apiKey)
                .build();
    }

    public Optional<TicketServerUser> verify(String email, String password) {
        VerifyResponse response;
        try {
            response = restClient.post()
                    .uri("/api/auth/verify")
                    .body(new VerifyRequest(email, password))
                    .retrieve()
                    .body(VerifyResponse.class);

        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.NotFound e) {
            return Optional.empty();                       // 인증 실패 = 정상적인 "아니오"

        } catch (RestClientException e) {
            log.warn("티켓 서버 사용자 인증 요청 실패: {}", e.getMessage());
            throw new TicketServerUnavailableException();  // 연결 실패, 5xx, 그 외
        }

        if (response == null || response.data() == null || response.data().isEmpty()) {
            return Optional.empty();
        }

        TicketServerUser user = response.data().get(0);
        if (user.userId() == null) {
            // 200 인데 userId 가 없으면 응답 형식이 바뀐 것. DB 에 null 넣지 않도록 여기서 막는다.
            log.warn("티켓 서버 verify 응답에 userId 가 없음: {}", user);
            throw new TicketServerUnavailableException();
        }
        return Optional.of(user);
    }

    record VerifyRequest(String email, String password) {
    }

    record VerifyResponse(boolean success, List<TicketServerUser> data) {
    }

    public record TicketServerUser(Long userId, String email, String name) {
    }
}