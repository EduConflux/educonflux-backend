package com.projects.EduConflux.classroom.meeting.dto;

import com.projects.EduConflux.classroom.meeting.entity.ParticipantRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingParticipantResponse {

    private Long id;
    private Long meetingId;
    private Long userId;
    private String userName;
    private String userEmail;
    private ParticipantRole role;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Integer durationSeconds;
    private boolean handRaised;
    private boolean micMuted;
    private boolean cameraOff;
}

