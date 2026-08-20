package com.projects.EduConflux.academic.controller;

import com.projects.EduConflux.academic.dto.SemesterRequest;
import com.projects.EduConflux.academic.dto.SemesterResponse;
import com.projects.EduConflux.academic.entity.SemesterStatus;
import com.projects.EduConflux.academic.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    public ResponseEntity<List<SemesterResponse>>
    getAllSemesters() {

        return ResponseEntity.ok(
                semesterService.getAllSemesters()
        );
    }

    @GetMapping("/program/{programId}")
    public ResponseEntity<List<SemesterResponse>>
    getSemestersByProgram(
            @PathVariable Long programId) {

        return ResponseEntity.ok(
                semesterService.getSemestersByProgram(
                        programId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SemesterResponse>
    getSemesterById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                semesterService.getSemesterById(id)
        );
    }

    @PostMapping
    public ResponseEntity<SemesterResponse>
    createSemester(
            @Valid @RequestBody SemesterRequest request) {

        return ResponseEntity.ok(
                semesterService.createSemester(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SemesterResponse>
    updateSemester(
            @PathVariable Long id,
            @Valid @RequestBody SemesterRequest request) {

        return ResponseEntity.ok(
                semesterService.updateSemester(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SemesterResponse>
    updateSemesterStatus(
            @PathVariable Long id,
            @RequestParam SemesterStatus status) {

        return ResponseEntity.ok(
                semesterService.updateSemesterStatus(
                        id,
                        status
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteSemester(
            @PathVariable Long id) {

        semesterService.deleteSemester(id);

        return ResponseEntity.noContent().build();
    }
}