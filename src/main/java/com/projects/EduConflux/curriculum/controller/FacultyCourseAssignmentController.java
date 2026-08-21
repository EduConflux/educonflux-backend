package com.projects.EduConflux.curriculum.controller;

import com.projects.EduConflux.curriculum.dto.FacultyCourseAssignmentRequest;
import com.projects.EduConflux.curriculum.dto.FacultyCourseAssignmentResponse;
import com.projects.EduConflux.curriculum.entity.FacultyCourseAssignmentStatus;
import com.projects.EduConflux.curriculum.service.FacultyCourseAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/faculty-course-assignments")
@RequiredArgsConstructor
public class FacultyCourseAssignmentController {

    private final FacultyCourseAssignmentService assignmentService;


    @GetMapping
    public ResponseEntity<List<FacultyCourseAssignmentResponse>>
    getAllAssignments() {

        return ResponseEntity.ok(
                assignmentService.getAllAssignments()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<FacultyCourseAssignmentResponse>
    getAssignmentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                assignmentService.getAssignmentById(id)
        );
    }


    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<FacultyCourseAssignmentResponse>>
    getAssignmentsByFaculty(
            @PathVariable Long facultyId) {

        return ResponseEntity.ok(
                assignmentService
                        .getAssignmentsByFaculty(facultyId)
        );
    }


    @GetMapping("/course-offering/{courseOfferingId}")
    public ResponseEntity<List<FacultyCourseAssignmentResponse>>
    getAssignmentsByCourseOffering(
            @PathVariable Long courseOfferingId) {

        return ResponseEntity.ok(
                assignmentService
                        .getAssignmentsByCourseOffering(
                                courseOfferingId
                        )
        );
    }


    @PostMapping
    public ResponseEntity<FacultyCourseAssignmentResponse>
    createAssignment(
            @Valid
            @RequestBody
            FacultyCourseAssignmentRequest request) {

        return ResponseEntity.ok(
                assignmentService
                        .createAssignment(request)
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<FacultyCourseAssignmentResponse>
    updateAssignment(
            @PathVariable Long id,
            @Valid
            @RequestBody
            FacultyCourseAssignmentRequest request) {

        return ResponseEntity.ok(
                assignmentService
                        .updateAssignment(
                                id,
                                request
                        )
        );
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<FacultyCourseAssignmentResponse>
    updateStatus(
            @PathVariable Long id,
            @RequestParam FacultyCourseAssignmentStatus status) {

        return ResponseEntity.ok(
                assignmentService
                        .updateStatus(
                                id,
                                status
                        )
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id) {

        assignmentService.deleteAssignment(id);

        return ResponseEntity.noContent().build();
    }
}