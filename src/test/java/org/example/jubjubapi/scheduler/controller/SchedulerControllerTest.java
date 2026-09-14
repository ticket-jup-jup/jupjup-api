package org.example.jubjubapi.scheduler.controller;

import org.example.jubjubapi.global.security.config.SecurityConfig;
import org.example.jubjubapi.global.security.jwt.JwtProvider;
import org.example.jubjubapi.performance.service.PerformanceService;
import org.example.jubjubapi.program.service.ProgramService;
import org.example.jubjubapi.ticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchedulerController.class)
@AutoConfigureRestDocs
@Import(SecurityConfig.class)
class SchedulerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProgramService programService;

    @MockitoBean
    PerformanceService performanceService;

    @MockitoBean
    TicketService ticketService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    void 프로그램_동기화() throws Exception {

        mockMvc.perform(
                        post("/api/internal/scheduler/program-sync")
                )
                .andExpect(status().isOk())
                .andDo(document(
                        "program-sync",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        verify(programService).getTicketServerProgram();
    }

    @Test
    void 회차_및_좌석_동기화() throws Exception {

        mockMvc.perform(
                        post("/api/internal/scheduler/performance-seat-sync")
                )
                .andExpect(status().isOk())
                .andDo(document(
                        "performance-seat-sync",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        verify(performanceService)
                .getTicketServerPerformanceAndSeat();
    }

    @Test
    void 티켓_동기화() throws Exception {

        mockMvc.perform(
                        post("/api/internal/scheduler/ticket-polling")
                )
                .andExpect(status().isOk())

                .andDo(document(
                        "ticket-polling",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        verify(ticketService).pollTickets();
    }
}