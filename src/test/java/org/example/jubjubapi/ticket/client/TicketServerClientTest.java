package org.example.jubjubapi.ticket.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DisplayName("티켓서버 클라이언트 테스트")
class TicketServerClientTest {

    private static final String TICKET_SERVER_URL = "http://localhost:8081";

    private TicketServerClient ticketServerClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.builder()
    }
}