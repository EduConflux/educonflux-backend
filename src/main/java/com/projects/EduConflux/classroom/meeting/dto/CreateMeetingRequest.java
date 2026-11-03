package com.projects.EduConflux.classroom.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMeetingRequest {

    @NotBlank(message = "Meeting title is required")
    @Size(max = 150, message = "Meeting title cannot exceed 150 characters")
    private String title;

    private LocalDateTime scheduledStartTime;

    @Builder.Default
    private Boolean allowStudentUnmute = true;

    @Builder.Default
    private Boolean allowStudentScreenShare = false;

    public boolean isAllowStudentUnmute() {
        return allowStudentUnmute == null || allowStudentUnmute;
    }

    public boolean isAllowStudentScreenShare() {
        return allowStudentScreenShare != null && allowStudentScreenShare;
    }
}
