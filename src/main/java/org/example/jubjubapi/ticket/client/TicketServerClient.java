package org.example.jubjubapi.ticket.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
}