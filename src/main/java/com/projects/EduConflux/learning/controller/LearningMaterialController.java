package com.projects.EduConflux.learning.controller;

import com.projects.EduConflux.learning.dto.CreateLearningMaterialRequest;
import com.projects.EduConflux.learning.dto.LearningMaterialResponse;
import com.projects.EduConflux.learning.service.LearningMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LearningMaterialController {

    private final LearningMaterialService materialService;


    /*
     * FACULTY
     *
     * Create learning material.
     */
    @PostMapping(
            "/faculty/classrooms/{classroomId}/learning-materials"
    )
    public ResponseEntity<LearningMaterialResponse>
    createMaterial(
            @PathVariable Long classroomId,
            @Valid @RequestBody
            CreateLearningMaterialRequest request) {

        return ResponseEntity.ok(
                materialService.createMaterial(
                        classroomId,
                        request
                )
        );
    }


    /*
     * FACULTY
     *
     * Update learning material.
     */
    @PutMapping(
            "/faculty/learning-materials/{materialId}"
    )
    public ResponseEntity<LearningMaterialResponse>
    updateMaterial(
            @PathVariable Long materialId,
            @Valid @RequestBody
            CreateLearningMaterialRequest request) {

        return ResponseEntity.ok(
                materialService.updateMaterial(
                        materialId,
                        request
                )
        );
    }


    /*
     * FACULTY
     *
     * Delete learning material.
     */
    @DeleteMapping(
            "/faculty/learning-materials/{materialId}"
    )
    public ResponseEntity<Void>
    deleteMaterial(
            @PathVariable Long materialId) {

        materialService.deleteMaterial(
                materialId
        );

        return ResponseEntity.noContent()
                .build();
    }


    /*
     * STUDENT
     *
     * View learning materials.
     */
    @GetMapping(
            "/student/classrooms/{classroomId}/learning-materials"
    )
    public ResponseEntity<Page<LearningMaterialResponse>>
    getStudentMaterials(
            @PathVariable Long classroomId,
            Pageable pageable) {

        return ResponseEntity.ok(
                materialService.getStudentMaterials(
                        classroomId,
                        pageable
                )
        );
    }


    /*
     * FACULTY
     *
     * View learning materials.
     */
    @GetMapping(
            "/faculty/classrooms/{classroomId}/learning-materials"
    )
    public ResponseEntity<Page<LearningMaterialResponse>>
    getFacultyMaterials(
            @PathVariable Long classroomId,
            Pageable pageable) {

        return ResponseEntity.ok(
                materialService.getFacultyMaterials(
                        classroomId,
                        pageable
                )
        );
    }
}