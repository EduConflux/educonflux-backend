package com.projects.EduConflux.assignment.submission.controller;

import com.projects.EduConflux.assignment.submission.dto.CreateSubmissionRequest;
import com.projects.EduConflux.assignment.submission.dto.GradeSubmissionRequest;
import com.projects.EduConflux.assignment.submission.dto.SubmissionResponse;
import com.projects.EduConflux.assignment.submission.service.AssignmentSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService submissionService;


    /*
     * STUDENT
     *
     * Submit an assignment.
     */
    @PostMapping("/student/assignments/submissions")
    public ResponseEntity<SubmissionResponse>
    createSubmission(
            @Valid @RequestBody
            CreateSubmissionRequest request) {

        return ResponseEntity.ok(
                submissionService
                        .createSubmission(request)
        );
    }


    /*
     * STUDENT
     *
     * View own submission.
     */
    @GetMapping(
            "/student/assignments/{assignmentId}/submission"
    )
    public ResponseEntity<SubmissionResponse>
    getMySubmission(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                submissionService
                        .getMySubmission(
                                assignmentId
                        )
        );
    }


    /*
     * FACULTY
     *
     * View all submissions for an assignment.
     */
    @GetMapping(
            "/faculty/assignments/{assignmentId}/submissions"
    )
    public ResponseEntity<List<SubmissionResponse>>
    getAssignmentSubmissions(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                submissionService
                        .getAssignmentSubmissions(
                                assignmentId
                        )
        );
    }

    /*
     * FACULTY
     *
     * Grade a student submission.
     */
    @PatchMapping(
            "/faculty/assignments/submissions/{submissionId}/grade"
    )
    public ResponseEntity<SubmissionResponse>
    gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody
            GradeSubmissionRequest request) {

        return ResponseEntity.ok(
                submissionService.gradeSubmission(
                        submissionId,
                        request
                )
        );
    }
}