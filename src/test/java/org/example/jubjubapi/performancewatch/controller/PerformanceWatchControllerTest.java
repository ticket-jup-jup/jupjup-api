package org.example.jubjubapi.performancewatch.controller;

import org.example.jubjubapi.global.security.config.SecurityConfig;
import org.example.jubjubapi.global.security.jwt.JwtAuthenticationToken;
import org.example.jubjubapi.global.security.jwt.JwtProvider;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
import org.example.jubjubapi.performancewatch.dto.PerformanceWatchResponse;
import org.example.jubjubapi.performancewatch.entity.PerformanceWatchStatus;
import org.example.jubjubapi.performancewatch.service.PerformanceWatchService;
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

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PerformanceWatchController.class)
@AutoConfigureRestDocs
@Import(SecurityConfig.class)
class PerformanceWatchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PerformanceWatchService performanceWatchService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    void 취소표_알림_구독_생성() throws Exception {

        LocalDateTime now =
                LocalDateTime.of(2026, 9, 15, 12, 0);

        PerformanceWatchResponse response =
                new PerformanceWatchResponse(
                        1L,
                        10L,
                        PerformanceWatchStatus.ACTIVE,
                        now,
                        now
                );

        given(
                performanceWatchService.createWatch(1L, 10L)
        ).willReturn(response);

        String body = """
            {
              "performanceId": 10
            }
            """;

        mockMvc.perform(
                        post("/api/performance-watches")
                                .with(authentication(userToken()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())

                .andDo(document(
                        "performance-watch-create",

                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("performanceId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("구독할 공연 회차 ID. 1 이상")
                        ),

                        responseFields(
                                fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),

                                fieldWithPath("data")
                                        .type(JsonFieldType.ARRAY)
                                        .description("생성된 구독"),

                                fieldWithPath("data[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("구독 ID"),

                                fieldWithPath("data[].performanceId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("공연 회차 ID"),

                                fieldWithPath("data[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("구독 상태"),

                                fieldWithPath("data[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("구독 생성 일시"),

                                fieldWithPath("data[].updatedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("구독 수정 일시"),

                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .optional()
                                        .description("성공 시 null")
                        )
                ));
    }

    private JwtAuthenticationToken userToken() {

        return new JwtAuthenticationToken(
                new JwtUserPrincipal(
                        1L,
                        "jupjup@example.com",
                        Role.USER
                )
        );
    }
}