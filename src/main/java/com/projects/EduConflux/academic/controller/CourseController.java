package com.projects.EduConflux.academic.controller;

import com.projects.EduConflux.academic.dto.CourseRequest;
import com.projects.EduConflux.academic.dto.CourseResponse;
import com.projects.EduConflux.academic.entity.CourseStatus;
import com.projects.EduConflux.academic.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<CourseResponse>>
    getAllCourses() {

        return ResponseEntity.ok(
                courseService.getAllCourses()
        );
    }

    @GetMapping("/program/{programId}")
    public ResponseEntity<List<CourseResponse>>
    getCoursesByProgram(
            @PathVariable Long programId) {

        return ResponseEntity.ok(
                courseService.getCoursesByProgram(
                        programId
                )
        );
    }

    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<List<CourseResponse>>
    getCoursesBySemester(
            @PathVariable Long semesterId) {

        return ResponseEntity.ok(
                courseService.getCoursesBySemester(
                        semesterId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse>
    getCourseById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                courseService.getCourseById(id)
        );
    }

    @PostMapping
    public ResponseEntity<CourseResponse>
    createCourse(
            @Valid @RequestBody CourseRequest request) {

        return ResponseEntity.ok(
                courseService.createCourse(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse>
    updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {

        return ResponseEntity.ok(
                courseService.updateCourse(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CourseResponse>
    updateCourseStatus(
            @PathVariable Long id,
            @RequestParam CourseStatus status) {

        return ResponseEntity.ok(
                courseService.updateCourseStatus(
                        id,
                        status
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteCourse(
            @PathVariable Long id) {

        courseService.deleteCourse(id);

        return ResponseEntity.noContent().build();
    }
}