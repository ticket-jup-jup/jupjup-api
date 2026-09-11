package org.example.jubjubapi.global.internal.controller;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.program.service.ProgramService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalSyncController {

    private final ProgramService programService;

    // 티켓서버 프로그램 수동 동기화
    @PostMapping("/programs/sync")
    public void syncPrograms() {
        programService.getTicketServerProgram();
    }
}
