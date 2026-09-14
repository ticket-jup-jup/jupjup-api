package org.example.jubjubapi.ticket.controller;

import org.example.jubjubapi.global.security.config.SecurityConfig;
import org.example.jubjubapi.global.security.jwt.JwtAuthenticationToken;
import org.example.jubjubapi.global.security.jwt.JwtProvider;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
import org.example.jubjubapi.ticket.dto.TicketResponse;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.service.TicketService;
import org.example.jubjubapi.user.entity.Role;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@AutoConfigureRestDocs
@Import(SecurityConfig.class)
class TicketControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TicketService ticketService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    void 티켓_목록_조회() throws Exception {

        LocalDateTime now = LocalDateTime.of(
                2026, 9, 15, 12, 0
        );

        TicketResponse response = new TicketResponse(
                1L,
                101L,
                BigDecimal.valueOf(150000),
                TicketStatus.AVAILABLE,
                now,
                now
        );

        given(
                ticketService.getTickets(
                        10L,
                        TicketStatus.AVAILABLE,
                        0,
                        20
                )
        ).willReturn(List.of(response));

        mockMvc.perform(
                        org.springframework.restdocs.mockmvc
                                .RestDocumentationRequestBuilders
                                .get("/api/tickets")

                                .param("performanceId", "10")
                                .param("status", "AVAILABLE")
                                .param("page", "0")
                                .param("size", "20")

                                .with(authentication(userToken()))
                )
                .andExpect(status().isOk())

                .andDo(document(
                        "ticket-list",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("performanceId")
                                        .optional()
                                        .description("조회할 공연 회차 ID"),

                                parameterWithName("status")
                                        .optional()
                                        .description("티켓 상태 (AVAILABLE, SOLD 등)"),

                                parameterWithName("page")
                                        .optional()
                                        .description("페이지 번호. 기본값 0"),

                                parameterWithName("size")
                                        .optional()
                                        .description("페이지 크기. 기본값 20")
                        ),

                        responseFields(
                                fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),

                                fieldWithPath("data")
                                        .type(JsonFieldType.ARRAY)
                                        .description("티켓 목록"),

                                fieldWithPath("data[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("줍줍 서버 티켓 ID"),

                                fieldWithPath("data[].externalTicketId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("티켓 서버의 티켓 ID"),

                                fieldWithPath("data[].price")
                                        .type(JsonFieldType.NUMBER)
                                        .description("티켓 가격"),

                                fieldWithPath("data[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("티켓 상태"),

                                fieldWithPath("data[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("생성 일시"),

                                fieldWithPath("data[].updatedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("수정 일시"),

                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("성공 시 null")
                        )
                ));
    }

    @Test
    void 티켓_단건_조회() throws Exception {

        LocalDateTime now =
                LocalDateTime.of(2026, 9, 15, 12, 0);

        TicketResponse response =
                new TicketResponse(
                        1L,
                        101L,
                        BigDecimal.valueOf(150000),
                        TicketStatus.AVAILABLE,
                        now,
                        now
                );

        given(ticketService.getTicket(1L))
                .willReturn(response);

        mockMvc.perform(
                        org.springframework.restdocs.mockmvc
                                .RestDocumentationRequestBuilders
                                .get("/api/tickets/{ticketId}", 1L)
                                .with(authentication(userToken()))
                )
                .andExpect(status().isOk())

                .andDo(document(
                        "ticket-detail",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("ticketId")
                                        .description("조회할 티켓 ID")
                        ),

                        responseFields(
                                fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),

                                fieldWithPath("data")
                                        .type(JsonFieldType.ARRAY)
                                        .description("티켓 정보"),

                                fieldWithPath("data[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("티켓 ID"),

                                fieldWithPath("data[].externalTicketId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("티켓 서버의 티켓 ID"),

                                fieldWithPath("data[].price")
                                        .type(JsonFieldType.NUMBER)
                                        .description("티켓 가격"),

                                fieldWithPath("data[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("티켓 상태"),

                                fieldWithPath("data[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("생성 일시"),

                                fieldWithPath("data[].updatedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("수정 일시"),

                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("성공 시 null")
                        )
                ));
    }

    @Test
    void 티켓_삭제() throws Exception {

        mockMvc.perform(
                        org.springframework.restdocs.mockmvc
                                .RestDocumentationRequestBuilders
                                .delete("/api/tickets/{ticketId}", 1L)

                                .with(authentication(adminToken()))
                )
                .andExpect(status().isNoContent())

                .andDo(document(
                        "ticket-delete",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("ticketId")
                                        .description("삭제할 티켓 ID")
                        )
                ));
    }

    //jwt USER/ADMIN
    private JwtAuthenticationToken userToken() {
        return new JwtAuthenticationToken(
                new JwtUserPrincipal(
                        1L,
                        "jupjup@example.com",
                        Role.USER
                )
        );
    }

    private JwtAuthenticationToken adminToken() {
        return new JwtAuthenticationToken(
                new JwtUserPrincipal(
                        1L,
                        "admin@jupjup.com",
                        Role.ADMIN
                )
        );
    }
}