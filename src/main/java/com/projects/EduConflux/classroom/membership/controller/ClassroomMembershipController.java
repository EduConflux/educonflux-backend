package com.projects.EduConflux.classroom.membership.controller;

import com.projects.EduConflux.classroom.membership.dto.MembershipResponse;
import com.projects.EduConflux.classroom.membership.dto.StudentClassroomResponse;
import com.projects.EduConflux.classroom.membership.service.ClassroomMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClassroomMembershipController {

    private final ClassroomMembershipService membershipService;


    @GetMapping("/api/student/classrooms")
    public ResponseEntity<List<StudentClassroomResponse>>
    getMyClassrooms() {

        return ResponseEntity.ok(
                membershipService.getMyClassrooms()
        );
    }


    @GetMapping(
            "/api/faculty/classrooms/{classroomId}/members"
    )
    public ResponseEntity<List<MembershipResponse>>
    getClassroomMembers(
            @PathVariable Long classroomId) {

        return ResponseEntity.ok(
                membershipService.getClassroomMembers(
                        classroomId
                )
        );
    }


    @PatchMapping(
            "/api/faculty/classrooms/{classroomId}/members/{studentId}/remove"
    )
    public ResponseEntity<Void> removeStudent(
            @PathVariable Long classroomId,
            @PathVariable Long studentId) {

        membershipService.removeStudent(
                classroomId,
                studentId
        );

        return ResponseEntity.noContent().build();
    }
}