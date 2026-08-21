package com.projects.EduConflux.student.controller;

import com.projects.EduConflux.student.dto.StudentRequest;
import com.projects.EduConflux.student.dto.StudentResponse;
import com.projects.EduConflux.student.entity.StudentStatus;
import com.projects.EduConflux.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {

        return ResponseEntity.ok(
                studentService.getAllStudents()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                studentService.getStudentById(id)
        );
    }

    @GetMapping("/program/{programId}")
    public ResponseEntity<List<StudentResponse>> getStudentsByProgram(
            @PathVariable Long programId) {

        return ResponseEntity.ok(
                studentService.getStudentsByProgram(programId)
        );
    }

    @GetMapping("/section/{classSectionId}")
    public ResponseEntity<List<StudentResponse>> getStudentsByClassSection(
            @PathVariable Long classSectionId) {

        return ResponseEntity.ok(
                studentService.getStudentsByClassSection(
                        classSectionId
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<StudentResponse>> getStudentsByStatus(
            @PathVariable StudentStatus status) {

        return ResponseEntity.ok(
                studentService.getStudentsByStatus(status)
        );
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(
            @Valid @RequestBody StudentRequest request) {

        return ResponseEntity.ok(
                studentService.createStudent(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {

        return ResponseEntity.ok(
                studentService.updateStudent(id, request)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<StudentResponse> updateStudentStatus(
            @PathVariable Long id,
            @RequestParam StudentStatus status) {

        return ResponseEntity.ok(
                studentService.updateStudentStatus(
                        id,
                        status
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id) {

        studentService.deleteStudent(id);

        return ResponseEntity.noContent().build();
    }
}