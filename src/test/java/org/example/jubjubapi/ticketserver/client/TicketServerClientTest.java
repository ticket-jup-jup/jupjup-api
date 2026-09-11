package org.example.jubjubapi.ticketserver.client;

import org.example.jubjubapi.performance.dto.TicketServerPerformance;
import org.example.jubjubapi.performance.entity.PerformanceStatus;
import org.example.jubjubapi.program.dto.TicketServerProgram;
import org.example.jubjubapi.program.entity.ProgramType;
import org.example.jubjubapi.seat.dto.TicketServerSeat;
import org.example.jubjubapi.ticket.exception.TicketException;
import org.example.jubjubapi.ticketserver.client.exception.TicketServerPerformanceDataNotFoundException;
import org.example.jubjubapi.ticketserver.client.exception.TicketServerProgramDataNotFoundException;
import org.example.jubjubapi.ticketserver.client.exception.TicketServerSeatDataNotFoundException;
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

import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    void 프로그램_목록_조회() {
        // given
        String responseJson = """
                {
                  "success": true,
                  "data": [
                    {
                      "id": 1,
                      "name": "뮤지컬 위키드",
                      "type": "MUSICAL",
                      "description": "뮤지컬 공연"
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo(TICKET_SERVER_URL + "/api/programs"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // when
        List<TicketServerProgram> result = ticketServerClient.getPrograms();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("뮤지컬 위키드");
        assertThat(result.get(0).getType()).isEqualTo(ProgramType.MUSICAL);
        assertThat(result.get(0).getDescription()).isEqualTo("뮤지컬 공연");

        mockServer.verify();
    }

    @Test
    void 프로그램_데이터_없음_예외() {
        // given
        String responseJson = """
                {
                  "success": true,
                  "data": []
                }
                """;

        mockServer.expect(requestTo(TICKET_SERVER_URL + "/api/programs"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> ticketServerClient.getPrograms()).isInstanceOf(TicketServerProgramDataNotFoundException.class);

        mockServer.verify();
    }

    @Test
    void 회차_목록_조회() {
        String responseJson = """
                {
                  "success": true,
                  "data": [
                    {
                      "id": 1,
                      "startAt": "2026-10-01T19:00:00",
                      "endAt": "2026-10-01T21:00:00",
                      "venue": "공연장",
                      "status": "UPCOMING"
                    }
                  ]
                }
                """;

        mockServer.expect(
                        requestTo(TICKET_SERVER_URL + "/api/performances?program=100")
                )
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess(responseJson, MediaType.APPLICATION_JSON)
                );

        List<TicketServerPerformance> result = ticketServerClient.getPerformances(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getStartAt()).isEqualTo(LocalDateTime.of(2026, 10, 1, 19, 0));
        assertThat(result.get(0).getEndAt()).isEqualTo(LocalDateTime.of(2026, 10, 1, 21, 0));
        assertThat(result.get(0).getVenue()).isEqualTo("공연장");
        assertThat(result.get(0).getStatus()).isEqualTo(PerformanceStatus.UPCOMING);

        mockServer.verify();
    }

    @Test
    void 회차_데이터_없음_예외() {
        String responseJson = """
                {
                  "success": true,
                  "data": []
                }
                """;

        mockServer.expect(
                        requestTo(TICKET_SERVER_URL + "/api/performances?program=100")
                )
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withSuccess(responseJson, MediaType.APPLICATION_JSON)
                );

        assertThatThrownBy(() -> ticketServerClient.getPerformances(100L))
                .isInstanceOf(TicketServerPerformanceDataNotFoundException.class);

        mockServer.verify();
    }

    @Test
    void 좌석_조회_성공() {
        // given
        String responseJson = """
                {
                    "success": true,
                    "data": [
                        {
                            "id": 1,
                            "performanceId": 100,
                            "section": "A",
                            "seatRow": "1",
                            "seatNumber": 1
                        },
                        {
                            "id": 2,
                            "performanceId": 100,
                            "section": "A",
                            "seatRow": "1",
                            "seatNumber": 2
                        }
                    ]
                }
                """;

        mockServer.expect(requestTo(TICKET_SERVER_URL + "/api/seats?performance=100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // when
        List<TicketServerSeat> result;
        result = ticketServerClient.getSeats(100L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getPerformanceId()).isEqualTo(100L);
        assertThat(result.get(0).getSection()).isEqualTo("A");
        assertThat(result.get(0).getSeatRow()).isEqualTo("1");
        assertThat(result.get(0).getSeatNumber()).isEqualTo(1);

        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getPerformanceId()).isEqualTo(100L);
        assertThat(result.get(1).getSection()).isEqualTo("A");
        assertThat(result.get(1).getSeatRow()).isEqualTo("1");
        assertThat(result.get(1).getSeatNumber()).isEqualTo(2);

        mockServer.verify();
    }

    @Test
    void 좌석_조회_데이터_없음() {
        // given
        String responseJson = """
                {
                    "success": true,
                    "data": []
                }
                """;

        mockServer.expect(requestTo(TICKET_SERVER_URL + "/api/seats?performance=100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> ticketServerClient.getSeats(100L))
                .isInstanceOf(TicketServerSeatDataNotFoundException.class);

        mockServer.verify();
    }

}