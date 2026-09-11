package org.example.jubjubapi.program.repository;

import org.example.jubjubapi.program.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Long> {
}
