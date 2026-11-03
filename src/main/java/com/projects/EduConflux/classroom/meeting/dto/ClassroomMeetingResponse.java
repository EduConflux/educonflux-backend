package com.projects.EduConflux.classroom.meeting.dto;

import com.projects.EduConflux.classroom.meeting.entity.MeetingStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomMeetingResponse {

    private Long id;
    private Long classroomId;
    private String classroomName;
    private Long hostUserId;
    private String hostName;
    private String title;
    private MeetingStatus status;
    private String roomCode;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private boolean allowStudentUnmute;
    private boolean allowStudentScreenShare;
    private long activeParticipantCount;
    private LocalDateTime createdAt;
}

