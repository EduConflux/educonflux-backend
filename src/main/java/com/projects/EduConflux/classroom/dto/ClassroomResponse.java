package com.projects.EduConflux.classroom.dto;

import com.projects.EduConflux.classroom.entity.ClassroomStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomResponse {

    private Long id;

    private Long institutionId;

    private Long facultyId;
    private String facultyName;

    private Long courseOfferingId;
    private Long courseId;
    private String courseCode;
    private String courseName;

    private Long classSectionId;
    private String classSectionName;

    private String name;
    private String description;

    private ClassroomStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}