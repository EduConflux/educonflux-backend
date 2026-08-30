package com.projects.EduConflux.files.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResponse {

    private Long id;

    private String originalName;

    private String contentType;

    private Long fileSize;

    private LocalDateTime createdAt;
}