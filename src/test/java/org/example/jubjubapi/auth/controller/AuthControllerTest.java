package org.example.jubjubapi.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.jubjubapi.auth.dto.SigninResponse;
import org.example.jubjubapi.auth.exception.DuplicateEmailException;
import org.example.jubjubapi.auth.service.AuthService;
import org.example.jubjubapi.global.security.config.SecurityConfig;
import org.example.jubjubapi.global.security.jwt.JwtAuthenticationToken;
import org.example.jubjubapi.global.security.jwt.JwtProvider;
import org.example.jubjubapi.global.security.jwt.JwtUserPrincipal;
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

@WebMvcTest(AuthController.class)
@AutoConfigureRestDocs
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    void 회원가입() throws Exception {


        String body = """
                {
                  "email": "jupjup@example.com",
                  "password": "password1!",
                  "name": "박준용"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")          // POST 요청 준비
                        .contentType(MediaType.APPLICATION_JSON)   // "본문은 JSON이다"
                        .content(body))                            // 본문 넣기
                .andExpect(status().isCreated())                   // 201인지 확인
                .andDo(document("auth-signup",            // 여기서부터 문서화
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(                             // 요청 필드 설명
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일. 이메일 형식, 255자 이하"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호. 8자 이상 64자 이하"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("이름. 30자 이하")
                        ),
                        responseFields(                            // 응답 필드 설명
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("반환 데이터 없음. 항상 빈 배열"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).optional().description("성공 시 null")
                        )
                ));
    }

    @Test
    void 회원가입_이메일_중복() throws Exception {
        String body = """
            {"email": "dup@example.com", "password": "password1!", "name": "박준용"}
            """;
        willThrow(new DuplicateEmailException()).given(authService).signup(any());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andDo(document("auth-signup-duplicate-email",
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("실패 시 false"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).optional().description("실패 시 null"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드. DUPLICATE_EMAIL"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    void 로그인() throws Exception {
        String body = """
            {"email": "jupjup@example.com", "password": "password1!"}
            """;
        given(authService.signin(any()))
                .willReturn(new SigninResponse("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.dummy-signature"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andDo(document("auth-signin",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data[].accessToken").type(JsonFieldType.STRING).description("발급된 JWT 액세스 토큰"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).optional().description("성공 시 null")
                        )
                ));
    }

    @Test
    void 로그아웃() throws Exception {
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                new JwtUserPrincipal(1L, "jupjup@example.com", Role.USER));

        mockMvc.perform(post("/api/auth/logout")
                        .with(authentication(token)))
                .andExpect(status().isOk())
                .andDo(document("auth-logout",
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("항상 빈 배열"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).optional().description("성공 시 null")
                        )
                ));
    }
}