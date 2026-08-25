package com.projects.EduConflux.classroom.post.dto;

import com.projects.EduConflux.classroom.post.entity.PostType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private Long id;

    private Long classroomId;
    private String classroomName;

    private Long facultyId;
    private String facultyName;

    private PostType type;

    private String title;
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}