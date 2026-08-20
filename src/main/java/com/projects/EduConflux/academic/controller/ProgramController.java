package com.projects.EduConflux.academic.controller;

import com.projects.EduConflux.academic.dto.ProgramRequest;
import com.projects.EduConflux.academic.dto.ProgramResponse;
import com.projects.EduConflux.academic.entity.ProgramStatus;
import com.projects.EduConflux.academic.service.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @GetMapping
    public ResponseEntity<List<ProgramResponse>>
    getAllPrograms() {

        return ResponseEntity.ok(
                programService.getAllPrograms()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramResponse>
    getProgramById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                programService.getProgramById(id)
        );
    }

    @PostMapping
    public ResponseEntity<ProgramResponse>
    createProgram(
            @Valid @RequestBody ProgramRequest request) {

        return ResponseEntity.ok(
                programService.createProgram(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgramResponse>
    updateProgram(
            @PathVariable Long id,
            @Valid @RequestBody ProgramRequest request) {

        return ResponseEntity.ok(
                programService.updateProgram(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProgramResponse>
    updateProgramStatus(
            @PathVariable Long id,
            @RequestParam ProgramStatus status) {

        return ResponseEntity.ok(
                programService.updateProgramStatus(
                        id,
                        status
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteProgram(
            @PathVariable Long id) {

        programService.deleteProgram(id);

        return ResponseEntity.noContent().build();
    }
}