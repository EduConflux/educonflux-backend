package com.projects.EduConflux.academic.controller;

import com.projects.EduConflux.academic.dto.AcademicYearRequest;
import com.projects.EduConflux.academic.dto.AcademicYearResponse;
import com.projects.EduConflux.academic.entity.AcademicYearStatus;
import com.projects.EduConflux.academic.service.AcademicYearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/academic-years")
@RequiredArgsConstructor
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    @GetMapping
    public ResponseEntity<List<AcademicYearResponse>> getAllAcademicYears() {

        return ResponseEntity.ok(
                academicYearService.getAllAcademicYears()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicYearResponse> getAcademicYearById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                academicYearService.getAcademicYearById(id)
        );
    }

    @PostMapping
    public ResponseEntity<AcademicYearResponse> createAcademicYear(
            @Valid @RequestBody AcademicYearRequest request) {

        return ResponseEntity.ok(
                academicYearService.createAcademicYear(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AcademicYearResponse> updateAcademicYear(
            @PathVariable Long id,
            @Valid @RequestBody AcademicYearRequest request) {

        return ResponseEntity.ok(
                academicYearService.updateAcademicYear(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AcademicYearResponse> updateAcademicYearStatus(
            @PathVariable Long id,
            @RequestParam AcademicYearStatus status) {

        return ResponseEntity.ok(
                academicYearService.updateAcademicYearStatus(
                        id,
                        status
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAcademicYear(
            @PathVariable Long id) {

        academicYearService.deleteAcademicYear(id);

        return ResponseEntity.noContent().build();
    }
}