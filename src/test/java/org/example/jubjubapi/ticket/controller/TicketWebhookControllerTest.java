package org.example.jubjubapi.ticket.controller;

import org.example.jubjubapi.global.security.config.SecurityConfig;
import org.example.jubjubapi.global.security.jwt.JwtProvider;
import org.example.jubjubapi.ticket.dto.TicketCanceledWebhookRequest;
import org.example.jubjubapi.ticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketWebhookController.class)
@AutoConfigureRestDocs
@Import(SecurityConfig.class)
class TicketWebhookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TicketService ticketService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    void 취소표_상태_webhook_수신() throws Exception {

        String body = """
                {
                  "ticketId": 101,
                  "performanceId": 10,
                  "seatId": 501,
                  "price": 150000,
                  "canceledAt": "2026-09-15T05:30:00"
                }
                """;

        mockMvc.perform(
                        post("/api/internal/webhooks/tickets/canceled")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())

                .andDo(document(
                        "ticket-webhook-canceled",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("ticketId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("티켓 서버의 티켓 ID"),

                                fieldWithPath("performanceId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("공연 회차 ID"),

                                fieldWithPath("seatId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("좌석 ID"),

                                fieldWithPath("price")
                                        .type(JsonFieldType.NUMBER)
                                        .description("취소된 티켓 가격"),

                                fieldWithPath("canceledAt")
                                        .type(JsonFieldType.STRING)
                                        .description("티켓 취소 발생 일시")
                        ),

                        responseFields(
                                fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),

                                fieldWithPath("data")
                                        .type(JsonFieldType.ARRAY)
                                        .description("반환 데이터 없음. 항상 빈 배열"),

                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("성공 시 null")
                        )
                ));

        verify(ticketService)
                .handleTicketCanceled(any(TicketCanceledWebhookRequest.class));
    }
}