package com.projects.EduConflux.classroom.post.dto;

import com.projects.EduConflux.classroom.post.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest {

    @NotNull(message = "Post type is required")
    private PostType type;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Content is required")
    private String content;
}