package com.projects.EduConflux.faculty.controller;

import com.projects.EduConflux.faculty.dto.FacultyRequest;
import com.projects.EduConflux.faculty.dto.FacultyResponse;
import com.projects.EduConflux.faculty.entity.FacultyStatus;
import com.projects.EduConflux.faculty.service.FacultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/faculty")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    @GetMapping
    public ResponseEntity<List<FacultyResponse>> getAllFaculty() {

        return ResponseEntity.ok(
                facultyService.getAllFaculty()
        );
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<FacultyResponse>> getFacultyByDepartment(
            @PathVariable Long departmentId) {

        return ResponseEntity.ok(
                facultyService.getFacultyByDepartment(
                        departmentId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyResponse> getFacultyById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                facultyService.getFacultyById(id)
        );
    }

    @PostMapping
    public ResponseEntity<FacultyResponse> createFaculty(
            @Valid @RequestBody FacultyRequest request) {

        return ResponseEntity.ok(
                facultyService.createFaculty(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyResponse> updateFaculty(
            @PathVariable Long id,
            @Valid @RequestBody FacultyRequest request) {

        return ResponseEntity.ok(
                facultyService.updateFaculty(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FacultyResponse> updateFacultyStatus(
            @PathVariable Long id,
            @RequestParam FacultyStatus status) {

        return ResponseEntity.ok(
                facultyService.updateFacultyStatus(
                        id,
                        status
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaculty(
            @PathVariable Long id) {

        facultyService.deleteFaculty(id);

        return ResponseEntity.noContent().build();
    }
}