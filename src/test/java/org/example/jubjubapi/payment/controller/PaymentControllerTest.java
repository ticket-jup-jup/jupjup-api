package org.example.jubjubapi.payment.controller;

import org.example.jubjubapi.global.security.config.SecurityConfig;
import org.example.jubjubapi.global.security.jwt.JwtAuthenticationToken;
import org.example.jubjubapi.global.security.jwt.JwtProvider;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
import org.example.jubjubapi.payment.dto.request.PaymentCreateRequest;
import org.example.jubjubapi.payment.dto.response.PaymentCancelResponse;
import org.example.jubjubapi.payment.dto.response.PaymentCreateResponse;
import org.example.jubjubapi.payment.dto.response.PaymentGetResponse;
import org.example.jubjubapi.payment.entity.PaymentMethod;
import org.example.jubjubapi.payment.entity.PaymentStatus;
import org.example.jubjubapi.payment.service.PaymentService;
import org.example.jubjubapi.payment.service.PaymentTransactionService;
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

@WebMvcTest(PaymentController.class)
@AutoConfigureRestDocs
@Import(SecurityConfig.class)
public class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PaymentService paymentService;

    @MockitoBean
    PaymentTransactionService paymentTransactionService;

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
    void 결제_요청() throws Exception {

        LocalDateTime paidAt =
                LocalDateTime.of(2026, 9, 16, 5, 30);

        PaymentCreateResponse response =
                PaymentCreateResponse.builder()
                        .id(1L)
                        .reservationId(10L)
                        .status(PaymentStatus.COMPLETED)
                        .amount(BigDecimal.valueOf(150000))
                        .paymentMethod(PaymentMethod.CARD)
                        .paidAt(paidAt)
                        .build();

        given(
                paymentService.pay(
                        eq(1L),
                        any(PaymentCreateRequest.class)
                )
        ).willReturn(response);

        String body = """
                {
                  "reservationId": 10,
                  "paymentMethod": "CARD"
                }
                """;

        mockMvc.perform(
                        post("/api/payments")
                                .with(authentication(userToken()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())

                .andDo(document(
                        "payment-create",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("reservationId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("결제할 예약 ID"),

                                fieldWithPath("paymentMethod")
                                        .type(JsonFieldType.STRING)
                                        .description("결제 수단. CARD 또는 CASH")
                        ),

                        responseFields(
                                fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),

                                fieldWithPath("data")
                                        .type(JsonFieldType.ARRAY)
                                        .description("결제 결과"),

                                fieldWithPath("data[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("결제 ID"),

                                fieldWithPath("data[].reservationId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("예약 ID"),

                                fieldWithPath("data[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("결제 상태"),

                                fieldWithPath("data[].amount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("결제 금액"),

                                fieldWithPath("data[].paymentMethod")
                                        .type(JsonFieldType.STRING)
                                        .description("결제 수단"),

                                fieldWithPath("data[].paidAt")
                                        .type(JsonFieldType.STRING)
                                        .description("결제 일시"),

                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("성공 시 null")
                        )
                ));
    }

    @Test
    void 결제_전체_조회() throws Exception {

        LocalDateTime paidAt =
                LocalDateTime.of(2026, 9, 16, 5, 30);

        TicketInfo ticketInfo =
                TicketInfo.builder()
                        .ticketId(100L)
                        .performanceId(200L)
                        .price(BigDecimal.valueOf(150000))
                        .build();

        PaymentGetResponse response =
                PaymentGetResponse.builder()
                        .id(1L)
                        .amount(BigDecimal.valueOf(150000))
                        .paymentMethod(PaymentMethod.CARD)
                        .status(PaymentStatus.COMPLETED)
                        .paidAt(paidAt)
                        .reservationId(10L)
                        .ticket(ticketInfo)
                        .build();

        given(
                paymentTransactionService.getAllPayment(
                        1L,
                        0,
                        10
                )
        ).willReturn(List.of(response));

        mockMvc.perform(
                        get("/api/payments")
                                .with(authentication(userToken()))
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())

                .andDo(document(
                        "payment-list",

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
                                        .description("결제 목록"),

                                fieldWithPath("data[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("결제 ID"),

                                fieldWithPath("data[].amount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("결제 금액"),

                                fieldWithPath("data[].paymentMethod")
                                        .type(JsonFieldType.STRING)
                                        .description("결제 수단"),

                                fieldWithPath("data[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("결제 상태"),

                                fieldWithPath("data[].paidAt")
                                        .type(JsonFieldType.STRING)
                                        .description("결제 일시"),

                                fieldWithPath("data[].reservationId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("예약 ID"),

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
    void 결제_단건_조회() throws Exception {

        LocalDateTime paidAt =
                LocalDateTime.of(2026, 9, 16, 5, 30);

        TicketInfo ticketInfo =
                TicketInfo.builder()
                        .ticketId(100L)
                        .performanceId(200L)
                        .price(BigDecimal.valueOf(150000))
                        .build();

        PaymentGetResponse response =
                PaymentGetResponse.builder()
                        .id(1L)
                        .amount(BigDecimal.valueOf(150000))
                        .paymentMethod(PaymentMethod.CARD)
                        .status(PaymentStatus.COMPLETED)
                        .paidAt(paidAt)
                        .reservationId(10L)
                        .ticket(ticketInfo)
                        .build();

        given(
                paymentTransactionService.getOnePayment(
                        1L,
                        1L
                )
        ).willReturn(response);

        mockMvc.perform(
                        get(
                                "/api/payments/{paymentId}",
                                1L
                        )
                                .with(authentication(userToken()))
                )
                .andExpect(status().isOk())

                .andDo(document(
                        "payment-detail",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("paymentId")
                                        .description("조회할 결제 ID")
                        ),

                        responseFields(
                                fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),

                                fieldWithPath("data")
                                        .type(JsonFieldType.ARRAY)
                                        .description("결제 정보"),

                                fieldWithPath("data[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("결제 ID"),

                                fieldWithPath("data[].amount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("결제 금액"),

                                fieldWithPath("data[].paymentMethod")
                                        .type(JsonFieldType.STRING)
                                        .description("결제 수단"),

                                fieldWithPath("data[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("결제 상태"),

                                fieldWithPath("data[].paidAt")
                                        .type(JsonFieldType.STRING)
                                        .description("결제 일시"),

                                fieldWithPath("data[].reservationId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("예약 ID"),

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
    void 결제_취소() throws Exception {

        PaymentCancelResponse response =
                PaymentCancelResponse.builder()
                        .id(1L)
                        .status(PaymentStatus.REFUNDED)
                        .amount(BigDecimal.valueOf(150000))
                        .build();

        given(
                paymentTransactionService.cancel(
                        1L,
                        1L
                )
        ).willReturn(response);

        mockMvc.perform(
                        post(
                                "/api/payments/{paymentId}/cancel",
                                1L
                        )
                                .with(authentication(userToken()))
                )
                .andExpect(status().isOk())

                .andDo(document(
                        "payment-cancel",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("paymentId")
                                        .description("취소할 결제 ID")
                        ),

                        responseFields(
                                fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),

                                fieldWithPath("data")
                                        .type(JsonFieldType.ARRAY)
                                        .description("환불 결과"),

                                fieldWithPath("data[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("결제 ID"),

                                fieldWithPath("data[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("결제 상태. 환불 완료 시 REFUNDED"),

                                fieldWithPath("data[].amount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("환불 금액"),

                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("성공 시 null")
                        )
                ));
    }
}


