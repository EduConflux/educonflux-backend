package com.projects.EduConflux.classroom.invitation.controller;

import com.projects.EduConflux.classroom.invitation.dto.InvitationResponse;
import com.projects.EduConflux.classroom.invitation.dto.JoinClassroomRequest;
import com.projects.EduConflux.classroom.invitation.service.ClassroomInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClassroomInvitationController {

    private final ClassroomInvitationService
            invitationService;


    /*
     * FACULTY
     *
     * Create invitation link.
     */
    @PostMapping(
            "/api/faculty/classrooms/{classroomId}/invitations"
    )
    public ResponseEntity<InvitationResponse>
    createInvitation(
            @PathVariable Long classroomId) {

        return ResponseEntity.ok(
                invitationService.createInvitation(
                        classroomId
                )
        );
    }


    /*
     * FACULTY
     *
     * View invitations created for
     * a classroom.
     */
    @GetMapping(
            "/api/faculty/classrooms/{classroomId}/invitations"
    )
    public ResponseEntity<List<InvitationResponse>>
    getClassroomInvitations(
            @PathVariable Long classroomId) {

        return ResponseEntity.ok(
                invitationService
                        .getClassroomInvitations(
                                classroomId
                        )
        );
    }


    /*
     * STUDENT
     *
     * Join classroom using invitation.
     */
    @PostMapping(
            "/api/student/classrooms/join"
    )
    public ResponseEntity<Void> joinClassroom(
            @Valid @RequestBody
            JoinClassroomRequest request) {

        invitationService.joinClassroom(
                request
        );

        return ResponseEntity.noContent()
                .build();
    }
}