package com.projects.EduConflux.assignment.controller;

import com.projects.EduConflux.assignment.dto.AssignmentResponse;
import com.projects.EduConflux.assignment.dto.CreateAssignmentRequest;
import com.projects.EduConflux.assignment.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;


    /*
     * FACULTY
     *
     * Create assignment.
     */
    @PostMapping(
            "/faculty/classrooms/{classroomId}/assignments"
    )
    public ResponseEntity<AssignmentResponse>
    createAssignment(
            @PathVariable Long classroomId,
            @Valid @RequestBody
            CreateAssignmentRequest request) {

        return ResponseEntity.ok(
                assignmentService.createAssignment(
                        classroomId,
                        request
                )
        );
    }


    /*
     * FACULTY
     *
     * View assignments created
     * for their classroom.
     */
    @GetMapping(
            "/faculty/classrooms/{classroomId}/assignments"
    )
    public ResponseEntity<Page<AssignmentResponse>>
    getFacultyAssignments(
            @PathVariable Long classroomId,
            Pageable pageable) {

        return ResponseEntity.ok(
                assignmentService.getFacultyAssignments(
                        classroomId,
                        pageable
                )
        );
    }


    /*
     * STUDENT
     *
     * View assignments for a classroom.
     */
    @GetMapping(
            "/student/classrooms/{classroomId}/assignments"
    )
    public ResponseEntity<Page<AssignmentResponse>>
    getStudentAssignments(
            @PathVariable Long classroomId,
            Pageable pageable) {

        return ResponseEntity.ok(
                assignmentService.getStudentAssignments(
                        classroomId,
                        pageable
                )
        );
    }
}