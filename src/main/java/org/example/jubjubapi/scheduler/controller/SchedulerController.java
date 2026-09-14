package org.example.jubjubapi.scheduler.controller;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.program.service.ProgramService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final ProgramService programService;

    @PostMapping("/program-sync")
    public void programSync() {
        programService.getTicketServerProgram();
    }
}