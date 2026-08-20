package com.projects.EduConflux.academic.controller;

import com.projects.EduConflux.academic.dto.ClassSectionRequest;
import com.projects.EduConflux.academic.dto.ClassSectionResponse;
import com.projects.EduConflux.academic.entity.ClassSectionStatus;
import com.projects.EduConflux.academic.service.ClassSectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/class-sections")
@RequiredArgsConstructor
public class ClassSectionController {

    private final ClassSectionService classSectionService;

    @GetMapping
    public ResponseEntity<List<ClassSectionResponse>>
    getAllClassSections() {

        return ResponseEntity.ok(
                classSectionService.getAllClassSections()
        );
    }

    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<List<ClassSectionResponse>>
    getClassSectionsBySemester(
            @PathVariable Long semesterId) {

        return ResponseEntity.ok(
                classSectionService
                        .getClassSectionsBySemester(
                                semesterId
                        )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassSectionResponse>
    getClassSectionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                classSectionService
                        .getClassSectionById(id)
        );
    }

    @PostMapping
    public ResponseEntity<ClassSectionResponse>
    createClassSection(
            @Valid @RequestBody ClassSectionRequest request) {

        return ResponseEntity.ok(
                classSectionService
                        .createClassSection(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassSectionResponse>
    updateClassSection(
            @PathVariable Long id,
            @Valid @RequestBody ClassSectionRequest request) {

        return ResponseEntity.ok(
                classSectionService.updateClassSection(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClassSectionResponse>
    updateClassSectionStatus(
            @PathVariable Long id,
            @RequestParam ClassSectionStatus status) {

        return ResponseEntity.ok(
                classSectionService
                        .updateClassSectionStatus(
                                id,
                                status
                        )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteClassSection(
            @PathVariable Long id) {

        classSectionService.deleteClassSection(id);

        return ResponseEntity.noContent().build();
    }
}