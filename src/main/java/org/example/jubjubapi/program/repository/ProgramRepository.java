package org.example.jubjubapi.program.repository;

import org.example.jubjubapi.program.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    Optional<Program> findByExternalProgramId(Long externalProgramId);

    List<Program> findAllByDeletedAtIsNullAndExternalProgramIdNotIn(Set<Long> externalProgramIds);

    List<Program> findAllByDeletedAtIsNull();
}
