package com.projects.EduConflux.parent.controller;

import com.projects.EduConflux.parent.dto.LinkStudentRequest;
import com.projects.EduConflux.parent.dto.ParentRequest;
import com.projects.EduConflux.parent.dto.ParentResponse;
import com.projects.EduConflux.parent.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.projects.EduConflux.attendance.dto.AttendanceResponse;
import com.projects.EduConflux.parent.dto.ParentAttendanceSummaryResponse;

import java.util.List;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @PostMapping
    public ResponseEntity<ParentResponse> createParent(
            @Valid @RequestBody ParentRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(parentService.createParent(request));
    }

    @PostMapping("/{parentId}/students")
    public ResponseEntity<ParentResponse> linkStudent(
            @PathVariable Long parentId,
            @Valid @RequestBody LinkStudentRequest request) {

        return ResponseEntity.ok(
                parentService.linkStudent(parentId, request));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ParentResponse>> getParentsForStudent(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                parentService.getParentsForStudent(studentId));
    }

    @GetMapping("/{parentId}/students")
    public ResponseEntity<List<ParentResponse>> getStudentsForParent(
            @PathVariable Long parentId) {

        return ResponseEntity.ok(
                parentService.getStudentsForParent(parentId));
    }


    @GetMapping("/me/students")
    public ResponseEntity<List<ParentResponse>> getMyStudents() {
        return ResponseEntity.ok(
                parentService.getMyStudents()
        );
    }


    @GetMapping("/me/students/{studentId}/attendance/summary")
    public ResponseEntity<ParentAttendanceSummaryResponse>
    getStudentAttendanceSummary(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                parentService.getStudentAttendanceSummary(studentId));
    }

    @GetMapping("/me/students/{studentId}/attendance")
    public ResponseEntity<List<AttendanceResponse>>
    getStudentAttendanceHistory(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                parentService.getStudentAttendanceHistory(studentId));
    }


}
