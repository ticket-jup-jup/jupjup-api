package org.example.jubjubapi.program.dto;

import lombok.Getter;
import org.example.jubjubapi.program.entity.ProgramType;

@Getter
public class TicketServerProgram {

    private final Long id;
    private final String name;
    private final ProgramType type;
    private final String description;

    public TicketServerProgram(Long id, String name, ProgramType type, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
    }
}