package com.projects.EduConflux.assignment.submission.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {

    private Long id;

    private Long assignmentId;

    private String assignmentTitle;

    private Long studentId;

    private String studentName;

    private Long fileId;

    private String fileName;

    private String fileContentType;

    private Long fileSize;

    private String fileUrl;

    private LocalDateTime submittedAt;

    private Integer marks;

    private String feedback;

    private LocalDateTime gradedAt;
}