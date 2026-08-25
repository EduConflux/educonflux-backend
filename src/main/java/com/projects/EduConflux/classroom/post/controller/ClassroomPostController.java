package com.projects.EduConflux.classroom.post.controller;

import com.projects.EduConflux.classroom.post.dto.CreatePostRequest;
import com.projects.EduConflux.classroom.post.dto.PostResponse;
import com.projects.EduConflux.classroom.post.service.ClassroomPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ClassroomPostController {

    private final ClassroomPostService postService;


    @PostMapping(
            "/api/faculty/classrooms/{classroomId}/posts"
    )
    public ResponseEntity<PostResponse> createPost(
            @PathVariable Long classroomId,
            @Valid @RequestBody CreatePostRequest request) {

        return ResponseEntity.ok(
                postService.createPost(
                        classroomId,
                        request
                )
        );
    }


    @GetMapping(
            "/api/faculty/classrooms/{classroomId}/posts"
    )
    public ResponseEntity<Page<PostResponse>> getFacultyPosts(
            @PathVariable Long classroomId,
            Pageable pageable) {

        return ResponseEntity.ok(
                postService.getFacultyPosts(
                        classroomId,
                        pageable
                )
        );
    }


    @GetMapping(
            "/api/student/classrooms/{classroomId}/posts"
    )
    public ResponseEntity<Page<PostResponse>> getStudentPosts(
            @PathVariable Long classroomId,
            Pageable pageable) {

        return ResponseEntity.ok(
                postService.getStudentPosts(
                        classroomId,
                        pageable
                )
        );
    }
}