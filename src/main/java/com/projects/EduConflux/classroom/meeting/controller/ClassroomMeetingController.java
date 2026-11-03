package com.projects.EduConflux.classroom.meeting.controller;

import com.projects.EduConflux.classroom.meeting.dto.*;
import com.projects.EduConflux.classroom.meeting.service.ClassroomMeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms/{classroomId}/meetings")
@RequiredArgsConstructor
public class ClassroomMeetingController {

    private final ClassroomMeetingService meetingService;

    @PostMapping
    public ResponseEntity<ClassroomMeetingResponse> startMeeting(
            @PathVariable Long classroomId,
            @Valid @RequestBody(required = false) CreateMeetingRequest request) {

        if (request == null) {
            request = new CreateMeetingRequest();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meetingService.startMeeting(classroomId, request));
    }

    @GetMapping("/active")
    public ResponseEntity<ClassroomMeetingResponse> getActiveMeeting(
            @PathVariable Long classroomId) {

        return meetingService.getActiveMeeting(classroomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{meetingId}/end")
    public ResponseEntity<ClassroomMeetingResponse> endMeeting(
            @PathVariable Long classroomId,
            @PathVariable Long meetingId) {

        return ResponseEntity.ok(meetingService.endMeeting(classroomId, meetingId));
    }

    @GetMapping("/{meetingId}/participants")
    public ResponseEntity<List<MeetingParticipantResponse>> getParticipants(
            @PathVariable Long classroomId,
            @PathVariable Long meetingId) {

        return ResponseEntity.ok(meetingService.getParticipants(meetingId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ClassroomMeetingResponse>> getMeetingHistory(
            @PathVariable Long classroomId) {

        return ResponseEntity.ok(meetingService.getMeetingHistory(classroomId));
    }

    @GetMapping("/ice-servers")
    public ResponseEntity<List<IceServerResponse>> getIceServers(
            @PathVariable Long classroomId) {

        return ResponseEntity.ok(meetingService.getIceServers());
    }
}

