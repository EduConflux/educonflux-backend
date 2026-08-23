package com.projects.EduConflux.classroom.membership.dto;

import com.projects.EduConflux.classroom.membership.entity.MembershipStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipResponse {

    private Long membershipId;

    private Long classroomId;
    private String classroomName;

    private Long studentId;
    private String studentName;
    private String enrollmentNumber;
    private String email;

    private MembershipStatus status;

    private LocalDateTime joinedAt;
    private LocalDateTime removedAt;
}