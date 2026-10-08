package com.projects.EduConflux.assignment.submission.service;

import com.projects.EduConflux.assignment.entity.Assignment;
import com.projects.EduConflux.assignment.repository.AssignmentRepository;
import com.projects.EduConflux.assignment.submission.dto.CreateSubmissionRequest;
import com.projects.EduConflux.assignment.submission.dto.GradeSubmissionRequest;
import com.projects.EduConflux.assignment.submission.dto.SubmissionResponse;
import com.projects.EduConflux.assignment.submission.entity.AssignmentSubmission;
import com.projects.EduConflux.assignment.submission.repository.AssignmentSubmissionRepository;
import com.projects.EduConflux.classroom.membership.entity.ClassroomMembership;
import com.projects.EduConflux.classroom.membership.entity.MembershipStatus;
import com.projects.EduConflux.classroom.membership.repository.ClassroomMembershipRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.files.entity.StoredFile;
import com.projects.EduConflux.files.repository.StoredFileRepository;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentSubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final StoredFileRepository storedFileRepository;
    private final ClassroomMembershipRepository membershipRepository;


    /*
     * STUDENT
     *
     * Submit an assignment.
     */
    @Transactional
    public SubmissionResponse createSubmission(
            CreateSubmissionRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Long userId =
                SecurityUtils.getCurrentUserId();


        /*
         * Find authenticated student.
         */
        String studentEmail =
                SecurityUtils.getCurrentUserEmail();

        Student student =
                studentRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                studentEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student profile not found"
                                ));


        /*
         * Make sure student belongs to
         * the authenticated institution.
         */
        if (!student
                .getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Student does not belong to this institution"
            );
        }


        /*
         * Find assignment.
         */
        Assignment assignment =
                assignmentRepository
                        .findById(
                                request.getAssignmentId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                ));


        /*
         * Check classroom membership.
         */
        ClassroomMembership membership =
                membershipRepository
                        .findByClassroomIdAndStudentId(
                                assignment
                                        .getClassroom()
                                        .getId(),
                                student.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this classroom"
                                ));


        /*
         * Student must be active.
         */
        if (membership.getStatus()
                != MembershipStatus.ACTIVE) {

            throw new RuntimeException(
                    "You are not an active member of this classroom"
            );
        }


        /*
         * Check whether student already
         * submitted this assignment.
         */
        if (submissionRepository
                .existsByAssignmentIdAndStudentId(
                        assignment.getId(),
                        student.getId()
                )) {

            throw new RuntimeException(
                    "You have already submitted this assignment"
            );
        }


        /*
         * Find submitted file.
         */
        StoredFile file =
                storedFileRepository
                        .findById(
                                request.getFileId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "File not found"
                                ));


        /*
         * Make sure this student uploaded
         * the file.
         */
        if (!file
                .getUploadedBy()
                .getId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You cannot submit this file"
            );
        }


        /*
         * Check due date.
         */
        if (LocalDateTime.now()
                .isAfter(
                        assignment.getDueDate()
                )) {

            throw new RuntimeException(
                    "Assignment submission deadline has passed"
            );
        }


        /*
         * Create submission.
         */
        AssignmentSubmission submission =
                AssignmentSubmission.builder()
                        .assignment(assignment)
                        .student(student)
                        .file(file)
                        .submittedAt(
                                LocalDateTime.now()
                        )
                        .build();


        AssignmentSubmission saved =
                submissionRepository.save(
                        submission
                );


        return mapToResponse(saved);
    }


    /*
     * STUDENT
     *
     * View own submission.
     */
    @Transactional(readOnly = true)
    public SubmissionResponse getMySubmission(
            Long assignmentId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String studentEmail =
                SecurityUtils.getCurrentUserEmail();

        Student student =
                studentRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                studentEmail
                        )
                        .orElse(null);

        if (student == null) {
            return null;
        }

        AssignmentSubmission submission =
                submissionRepository
                        .findByAssignmentIdAndStudentId(
                                assignmentId,
                                student.getId()
                        )
                        .orElse(null);

        if (submission == null) {
            return null;
        }

        return mapToResponse(submission);
    }

    /*
     * STUDENT
     *
     * Remove / unsubmit own assignment submission.
     */
    @Transactional
    public void deleteMySubmission(Long assignmentId) {
        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String studentEmail =
                SecurityUtils.getCurrentUserEmail();

        Student student =
                studentRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                studentEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student profile not found"
                                ));

        AssignmentSubmission submission =
                submissionRepository
                        .findByAssignmentIdAndStudentId(
                                assignmentId,
                                student.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Submission not found"
                                ));

        // Check if submission is already graded
        if (submission.getMarks() != null) {
            throw new RuntimeException(
                    "Cannot remove submission because it has already been graded"
            );
        }

        submissionRepository.delete(submission);
    }


    /*
     * FACULTY
     *
     * View all submissions for an assignment.
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponse>
    getAssignmentSubmissions(
            Long assignmentId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String facultyEmail =
                SecurityUtils.getCurrentUserEmail();


        /*
         * Find authenticated faculty.
         */
        Faculty faculty =
                facultyRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                facultyEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty profile not found"
                                ));


        /*
         * Find assignment.
         */
        Assignment assignment =
                assignmentRepository
                        .findById(
                                assignmentId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                ));


        /*
         * Only classroom owner can
         * view submissions.
         */
        if (!assignment
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this assignment"
            );
        }


        return submissionRepository
                .findByAssignmentId(
                        assignmentId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    /*
     * Convert entity to response.
     */
    private SubmissionResponse mapToResponse(
            AssignmentSubmission submission) {

        Assignment assignment =
                submission.getAssignment();

        Student student =
                submission.getStudent();

        StoredFile file =
                submission.getFile();


        return SubmissionResponse.builder()

                .id(
                        submission.getId()
                )

                .assignmentId(
                        assignment.getId()
                )

                .assignmentTitle(
                        assignment.getTitle()
                )

                .studentId(
                        student.getId()
                )

                .studentName(
                        student.getFirstName()
                                + " "
                                + student.getLastName()
                )

                .fileId(
                        file != null ? file.getId() : null
                )

                .fileName(
                        file != null ? file.getOriginalName() : null
                )

                .fileContentType(
                        file != null ? file.getContentType() : null
                )

                .fileSize(
                        file != null ? file.getFileSize() : null
                )

                .fileUrl(
                        file != null ? file.getFilePath() : null
                )

                .submittedAt(
                        submission.getSubmittedAt()
                )

                .marks(
                        submission.getMarks()
                )

                .feedback(
                        submission.getFeedback()
                )

                .gradedAt(
                        submission.getGradedAt()
                )

                .build();
    }

    /*
     * FACULTY
     *
     * Grade a student submission.
     */
    @Transactional
    public SubmissionResponse gradeSubmission(
            Long submissionId,
            GradeSubmissionRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String facultyEmail =
                SecurityUtils.getCurrentUserEmail();


        /*
         * Find authenticated faculty.
         */
        Faculty faculty =
                facultyRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                facultyEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty profile not found"
                                ));


        /*
         * Find submission.
         */
        AssignmentSubmission submission =
                submissionRepository
                        .findById(
                                submissionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Submission not found"
                                ));


        Assignment assignment =
                submission.getAssignment();


        /*
         * Make sure the faculty owns
         * the assignment.
         */
        if (!assignment
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this assignment"
            );
        }


        /*
         * Make sure marks do not exceed
         * the maximum marks.
         */
        if (request.getMarks()
                > assignment.getMaxMarks()) {

            throw new RuntimeException(
                    "Marks cannot exceed maximum marks of "
                            + assignment.getMaxMarks()
            );
        }


        /*
         * Save marks and feedback.
         */
        submission.setMarks(
                request.getMarks()
        );

        submission.setFeedback(
                request.getFeedback()
        );

        submission.setGradedAt(
                LocalDateTime.now()
        );


        AssignmentSubmission saved =
                submissionRepository.save(
                        submission
                );


        return mapToResponse(saved);
    }
}