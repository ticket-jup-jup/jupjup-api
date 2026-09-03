package org.example.jubjubapi.ticket.client;

import org.example.jubjubapi.ticket.exception.TicketException;
import org.example.jubjubapi.ticketserver.exception.TicketServerUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@DisplayName("티켓서버 클라이언트 테스트")
class TicketServerClientTest {

    private static final String TICKET_SERVER_URL = "http://localhost:8081";

    private TicketServerClient ticketServerClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(TICKET_SERVER_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();

        ticketServerClient = new TicketServerClient(RestClient.builder(), TICKET_SERVER_URL, "test-api-key", new SimpleClientHttpRequestFactory());
        ReflectionTestUtils.setField(ticketServerClient, "restClient", builder.build());
    }

    @Test
    @DisplayName("임시예약 성공 시 티켓서버가 발급한 예약 id를 반환한다.")
    void 임시예약_성공() {
        //given
        String responseJson = """
                {
                  "success": true,
                  "data": [{
                    "reservation": {
                      "reservationId": 5,
                      "userId": 1,
                      "ticketId": 1,
                      "status": "PENDING",
                      "expiresAt": "2026-09-03T14:18:05",
                      "createdAt": "2026-09-03T14:08:05",
                      "updatedAt": "2026-09-03T14:08:05"
                    },
                    "payment": null
                  }],
                  "error": null
                }
                """;

        mockServer.expect(requestTo(TICKET_SERVER_URL + "/api/reservations"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));
        //when
        Long externalReservationId = ticketServerClient.createTemporaryReservation(1L, 1L);

        //then
        assertThat(externalReservationId).isEqualTo(5L);
        mockServer.verify();
    }

    @Test
    @DisplayName("티켓서버가 4xx 에러를 반환하면 예약 불가 예외가 발생한다.")
    void 임시예약_거부() {
        mockServer.expect(requestTo(TICKET_SERVER_URL + "/api/reservations"))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> ticketServerClient.createTemporaryReservation(1L, 1L))
                .isInstanceOf(TicketException.class);
    }

    @Test
    @DisplayName("티켓서버가 5xx 에러를 반환하면 통신 실패 예외가 발생한다.")
    void 티켓서버_장애() {
        mockServer.expect(requestTo(TICKET_SERVER_URL + "/api/reservations"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> ticketServerClient.createTemporaryReservation(1L, 1L))
                .isInstanceOf(TicketServerUnavailableException.class);
    }
}