package com.projects.EduConflux.classroom.controller;

import com.projects.EduConflux.classroom.dto.ClassroomRequest;
import com.projects.EduConflux.classroom.dto.ClassroomResponse;
import com.projects.EduConflux.classroom.service.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    public ResponseEntity<ClassroomResponse> createClassroom(
            @Valid @RequestBody ClassroomRequest request) {

        return ResponseEntity.ok(
                classroomService.createClassroom(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<ClassroomResponse>> getMyClassrooms() {

        return ResponseEntity.ok(
                classroomService.getMyClassrooms()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomResponse> getClassroomById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                classroomService.getClassroomById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassroomResponse> updateClassroom(
            @PathVariable Long id,
            @Valid @RequestBody ClassroomRequest request) {

        return ResponseEntity.ok(
                classroomService.updateClassroom(id, request)
        );
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archiveClassroom(
            @PathVariable Long id) {

        classroomService.archiveClassroom(id);

        return ResponseEntity.noContent().build();
    }
}