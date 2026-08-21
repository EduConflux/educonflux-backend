package com.projects.EduConflux.curriculum.controller;

import com.projects.EduConflux.curriculum.dto.StudentCourseEnrollmentRequest;
import com.projects.EduConflux.curriculum.dto.StudentCourseEnrollmentResponse;
import com.projects.EduConflux.curriculum.entity.StudentCourseEnrollmentStatus;
import com.projects.EduConflux.curriculum.service.StudentCourseEnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/student-course-enrollments")
@RequiredArgsConstructor
public class StudentCourseEnrollmentController {

    private final StudentCourseEnrollmentService enrollmentService;


    @GetMapping
    public ResponseEntity<List<StudentCourseEnrollmentResponse>>
    getAllEnrollments() {

        return ResponseEntity.ok(
                enrollmentService.getAllEnrollments()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<StudentCourseEnrollmentResponse>
    getEnrollmentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                enrollmentService.getEnrollmentById(id)
        );
    }


    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentCourseEnrollmentResponse>>
    getEnrollmentsByStudent(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                enrollmentService
                        .getEnrollmentsByStudent(studentId)
        );
    }


    @GetMapping("/course-offering/{courseOfferingId}")
    public ResponseEntity<List<StudentCourseEnrollmentResponse>>
    getEnrollmentsByCourseOffering(
            @PathVariable Long courseOfferingId) {

        return ResponseEntity.ok(
                enrollmentService
                        .getEnrollmentsByCourseOffering(
                                courseOfferingId
                        )
        );
    }


    @PostMapping
    public ResponseEntity<StudentCourseEnrollmentResponse>
    createEnrollment(
            @Valid
            @RequestBody
            StudentCourseEnrollmentRequest request) {

        return ResponseEntity.ok(
                enrollmentService
                        .createEnrollment(request)
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<StudentCourseEnrollmentResponse>
    updateEnrollment(
            @PathVariable Long id,
            @Valid
            @RequestBody
            StudentCourseEnrollmentRequest request) {

        return ResponseEntity.ok(
                enrollmentService
                        .updateEnrollment(
                                id,
                                request
                        )
        );
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<StudentCourseEnrollmentResponse>
    updateStatus(
            @PathVariable Long id,
            @RequestParam StudentCourseEnrollmentStatus status) {

        return ResponseEntity.ok(
                enrollmentService
                        .updateStatus(
                                id,
                                status
                        )
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(
            @PathVariable Long id) {

        enrollmentService.deleteEnrollment(id);

        return ResponseEntity.noContent().build();
    }
}