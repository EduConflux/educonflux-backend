package com.projects.EduConflux.curriculum.controller;

import com.projects.EduConflux.curriculum.dto.CourseOfferingRequest;
import com.projects.EduConflux.curriculum.dto.CourseOfferingResponse;
import com.projects.EduConflux.curriculum.entity.CourseOfferingStatus;
import com.projects.EduConflux.curriculum.service.CourseOfferingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/course-offerings")
@RequiredArgsConstructor
public class CourseOfferingController {

    private final CourseOfferingService courseOfferingService;


    @GetMapping
    public ResponseEntity<List<CourseOfferingResponse>> getAllOfferings() {

        return ResponseEntity.ok(
                courseOfferingService.getAllOfferings()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<CourseOfferingResponse> getOfferingById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                courseOfferingService.getOfferingById(id)
        );
    }


    @GetMapping("/academic-year/{academicYearId}")
    public ResponseEntity<List<CourseOfferingResponse>>
    getOfferingsByAcademicYear(
            @PathVariable Long academicYearId) {

        return ResponseEntity.ok(
                courseOfferingService
                        .getOfferingsByAcademicYear(
                                academicYearId
                        )
        );
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<List<CourseOfferingResponse>>
    getOfferingsByStatus(
            @PathVariable CourseOfferingStatus status) {

        return ResponseEntity.ok(
                courseOfferingService
                        .getOfferingsByStatus(status)
        );
    }


    @PostMapping
    public ResponseEntity<CourseOfferingResponse>
    createOffering(
            @Valid @RequestBody CourseOfferingRequest request) {

        return ResponseEntity.ok(
                courseOfferingService
                        .createOffering(request)
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<CourseOfferingResponse>
    updateOffering(
            @PathVariable Long id,
            @Valid @RequestBody CourseOfferingRequest request) {

        return ResponseEntity.ok(
                courseOfferingService
                        .updateOffering(
                                id,
                                request
                        )
        );
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<CourseOfferingResponse>
    updateStatus(
            @PathVariable Long id,
            @RequestParam CourseOfferingStatus status) {

        return ResponseEntity.ok(
                courseOfferingService
                        .updateStatus(
                                id,
                                status
                        )
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffering(
            @PathVariable Long id) {

        courseOfferingService.deleteOffering(id);

        return ResponseEntity.noContent().build();
    }
}