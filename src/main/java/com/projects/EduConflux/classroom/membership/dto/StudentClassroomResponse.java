package com.projects.EduConflux.classroom.membership.dto;

import com.projects.EduConflux.classroom.entity.ClassroomStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentClassroomResponse {

    private Long classroomId;

    private String classroomName;

    private String description;

    private Long facultyId;

    private String facultyName;

    private Long courseId;

    private String courseCode;

    private String courseName;

    private Long classSectionId;

    private String classSectionName;

    private ClassroomStatus status;
}