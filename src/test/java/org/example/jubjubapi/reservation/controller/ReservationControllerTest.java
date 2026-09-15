package org.example.jubjubapi.reservation.controller;

import org.example.jubjubapi.global.security.config.SecurityConfig;
import org.example.jubjubapi.global.security.jwt.JwtAuthenticationToken;
import org.example.jubjubapi.global.security.jwt.JwtProvider;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
import org.example.jubjubapi.reservation.dto.request.ReservationCreateRequest;
import org.example.jubjubapi.reservation.dto.response.ReservationCancelResponse;
import org.example.jubjubapi.reservation.dto.response.ReservationCreateResponse;
import org.example.jubjubapi.reservation.dto.response.ReservationGetResponse;
import org.example.jubjubapi.reservation.entity.ReservationStatus;
import org.example.jubjubapi.reservation.service.ReservationService;
import org.example.jubjubapi.reservation.service.ReservationTransactionService;
import org.example.jubjubapi.ticket.dto.TicketInfo;
import org.example.jubjubapi.user.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ReservationController.class)
@AutoConfigureRestDocs
@Import(SecurityConfig.class)
class ReservationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReservationService reservationService;

    @MockitoBean
    ReservationTransactionService reservationTransactionService;

    @MockitoBean
    JwtProvider jwtProvider;

    private JwtAuthenticationToken userToken() {
        return new JwtAuthenticationToken(
                new JwtUserPrincipal(
                        1L,
                        "jupjup@example.com",
                        Role.USER
                )
        );
    }

    @Test
    void 취소표_예약() throws Exception {

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 9, 16, 5, 0);

        LocalDateTime expiresAt =
                createdAt.plusMinutes(10);

        ReservationCreateResponse response =
                ReservationCreateResponse.builder()
                        .reservationId(1L)
                        .ticketId(10L)
                        .status(ReservationStatus.PENDING)
                        .expiresAt(expiresAt)
                        .createdAt(createdAt)
                        .build();

        given(
                reservationService.reserve(
                        eq(1L),
                        any(ReservationCreateRequest.class)
                )
        ).willReturn(response);

        String body = """
                {
                  "ticketId": 10
                }
                """;

        mockMvc.perform(
                        post("/api/reservations")
                                .with(authentication(userToken()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())

                .andDo(document(
                        "reservation-create",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("ticketId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("예약할 티켓 ID")
                        ),

                        responseFields(
                                fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),

                                fieldWithPath("data")
                                        .type(JsonFieldType.ARRAY)
                                        .description("생성된 예약 정보"),

                                fieldWithPath("data[].reservationId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("예약 ID"),

                                fieldWithPath("data[].ticketId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("예약한 티켓 ID"),

                                fieldWithPath("data[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("예약 상태"),

                                fieldWithPath("data[].expiresAt")
                                        .type(JsonFieldType.STRING)
                                        .description("임시 예약 만료 일시"),

                                fieldWithPath("data[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("예약 생성 일시"),

                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("성공 시 null")
                        )
                ));
    }

    @Test
    void 예약_단건_조회() throws Exception {

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 9, 16, 5, 0);

        LocalDateTime expiresAt =
                createdAt.plusMinutes(10);

        TicketInfo ticketInfo =
                TicketInfo.builder()
                        .ticketId(10L)
                        .performanceId(100L)
                        .price(BigDecimal.valueOf(150000))
                        .build();

        ReservationGetResponse response =
                ReservationGetResponse.builder()
                        .id(1L)
                        .status(ReservationStatus.PENDING)
                        .expiresAt(expiresAt)
                        .createdAt(createdAt)
                        .ticket(ticketInfo)
                        .build();

        given(
                reservationTransactionService.getReservation(
                        1L,
                        1L
                )
        ).willReturn(response);

        mockMvc.perform(
                        get(
                                "/api/reservations/{reservationId}",
                                1L
                        )
                                .with(authentication(userToken()))
                )
                .andExpect(status().isOk())

                .andDo(document(
                        "reservation-detail",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("reservationId")
                                        .description("조회할 예약 ID")
                        ),

                        responseFields(
                                fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),

                                fieldWithPath("data")
                                        .type(JsonFieldType.ARRAY)
                                        .description("예약 정보"),

                                fieldWithPath("data[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("예약 ID"),

                                fieldWithPath("data[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("예약 상태"),

                                fieldWithPath("data[].expiresAt")
                                        .type(JsonFieldType.STRING)
                                        .description("예약 만료 일시"),

                                fieldWithPath("data[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("예약 생성 일시"),

                                fieldWithPath("data[].ticket.ticketId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("티켓 ID"),

                                fieldWithPath("data[].ticket.performanceId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("공연 회차 ID"),

                                fieldWithPath("data[].ticket.price")
                                        .type(JsonFieldType.NUMBER)
                                        .description("티켓 가격"),

                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("성공 시 null")
                        )
                ));
    }

    @Test
    void 내_예약_목록_조회() throws Exception {

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 9, 16, 5, 0);

        LocalDateTime expiresAt =
                createdAt.plusMinutes(10);

        TicketInfo ticketInfo =
                TicketInfo.builder()
                        .ticketId(10L)
                        .performanceId(100L)
                        .price(BigDecimal.valueOf(150000))
                        .build();

        ReservationGetResponse response =
                ReservationGetResponse.builder()
                        .id(1L)
                        .status(ReservationStatus.PENDING)
                        .expiresAt(expiresAt)
                        .createdAt(createdAt)
                        .ticket(ticketInfo)
                        .build();

        given(
                reservationTransactionService.getMyReservation(
                        1L,
                        0,
                        10
                )
        ).willReturn(List.of(response));

        mockMvc.perform(
                        get("/api/reservations/my")
                                .with(authentication(userToken()))
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())

                .andDo(document(
                        "reservation-list",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("page")
                                        .optional()
                                        .description("페이지 번호. 기본값 0"),

                                parameterWithName("size")
                                        .optional()
                                        .description("페이지 크기. 기본값 10")
                        ),

                        responseFields(
                                fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),

                                fieldWithPath("data")
                                        .type(JsonFieldType.ARRAY)
                                        .description("예약 목록"),

                                fieldWithPath("data[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("예약 ID"),

                                fieldWithPath("data[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("예약 상태"),

                                fieldWithPath("data[].expiresAt")
                                        .type(JsonFieldType.STRING)
                                        .description("예약 만료 일시"),

                                fieldWithPath("data[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("예약 생성 일시"),

                                fieldWithPath("data[].ticket.ticketId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("티켓 ID"),

                                fieldWithPath("data[].ticket.performanceId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("공연 회차 ID"),

                                fieldWithPath("data[].ticket.price")
                                        .type(JsonFieldType.NUMBER)
                                        .description("티켓 가격"),

                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("성공 시 null")
                        )
                ));
    }
    @Test
    void 예약_취소() throws Exception {

        ReservationCancelResponse response =
                ReservationCancelResponse.builder()
                        .id(1L)
                        .status(ReservationStatus.CANCELLED)
                        .build();

        given(
                reservationTransactionService.cancel(
                        1L,
                        1L
                )
        ).willReturn(response);

        mockMvc.perform(
                        post(
                                "/api/reservations/{reservationId}/cancel",
                                1L
                        )
                                .with(authentication(userToken()))
                )
                .andExpect(status().isOk())

                .andDo(document(
                        "reservation-cancel",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("reservationId")
                                        .description("취소할 예약 ID")
                        ),

                        responseFields(
                                fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),

                                fieldWithPath("data")
                                        .type(JsonFieldType.ARRAY)
                                        .description("취소된 예약 정보"),

                                fieldWithPath("data[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("예약 ID"),

                                fieldWithPath("data[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("예약 상태. 취소 완료 시 CANCELLED"),

                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("성공 시 null")
                        )
                ));
    }
}






