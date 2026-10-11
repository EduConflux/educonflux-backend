package com.projects.EduConflux.assignment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentResponse {

    private Long id;

    private Long classroomId;

    private String classroomName;

    private Long facultyId;

    private String facultyName;

    private String title;

    private String description;

    private LocalDateTime dueDate;

    private Integer maxMarks;

    private Long fileId;

    private String fileName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Student submission and grading details
    private String submissionStatus;

    private LocalDateTime submittedAt;

    private Integer marksAwarded;

    private String remarks;

    private LocalDateTime gradedAt;
}
