package com.projects.EduConflux.assignment.service;

import com.projects.EduConflux.assignment.dto.AssignmentResponse;
import com.projects.EduConflux.assignment.dto.CreateAssignmentRequest;
import com.projects.EduConflux.assignment.entity.Assignment;
import com.projects.EduConflux.assignment.repository.AssignmentRepository;
import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.membership.entity.ClassroomMembership;
import com.projects.EduConflux.classroom.membership.entity.MembershipStatus;
import com.projects.EduConflux.classroom.membership.repository.ClassroomMembershipRepository;
import com.projects.EduConflux.classroom.repository.ClassroomRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.files.entity.StoredFile;
import com.projects.EduConflux.files.repository.StoredFileRepository;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.notification.entity.NotificationType;
import com.projects.EduConflux.notification.service.NotificationService;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomMembershipRepository membershipRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final StoredFileRepository storedFileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    /*
     * FACULTY
     *
     * Create an assignment for a classroom.
     */
    @Transactional
    public AssignmentResponse createAssignment(
            Long classroomId,
            CreateAssignmentRequest request) {

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
         * Find classroom inside the
         * authenticated institution.
         */
        Classroom classroom =
                classroomRepository
                        .findByIdAndInstitutionId(
                                classroomId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Classroom not found"
                                ));


        /*
         * Only classroom owner can create
         * assignments.
         */
        if (!classroom
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this classroom"
            );
        }


        /*
         * Find the uploaded file if a file
         * was provided.
         */
        StoredFile file = null;

        if (request.getFileId() != null) {

            file =
                    storedFileRepository
                            .findById(
                                    request.getFileId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "File not found"
                                    ));


            /*
             * Make sure the faculty member
             * attaching the file actually
             * uploaded it.
             */
            if (!file
                    .getUploadedBy()
                    .getId()
                    .equals(
                            SecurityUtils.getCurrentUserId()
                    )) {

                throw new RuntimeException(
                        "You cannot attach this file"
                );
            }
        }


        /*
         * Create assignment.
         */
        Assignment assignment =
                Assignment.builder()
                        .classroom(classroom)
                        .faculty(faculty)
                        .file(file)
                        .title(request.getTitle())
                        .description(request.getDescription())
                        .dueDate(request.getDueDate())
                        .maxMarks(request.getMaxMarks())
                        .build();


        Assignment savedAssignment =
                assignmentRepository.save(
                        assignment
                );


        /*
         * Create assignment notifications
         * for all active students in the classroom.
         */
        List<ClassroomMembership> memberships =
                membershipRepository
                        .findByClassroomIdAndStatus(
                                classroomId,
                                MembershipStatus.ACTIVE
                        );


        for (ClassroomMembership membership :
                memberships) {

            Student student =
                    membership.getStudent();


            /*
             * Find the User account belonging
             * to this student.
             */
            User user =
                    userRepository
                            .findByInstitutionIdAndEmail(
                                    institutionId,
                                    student.getEmail()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User account not found for student: "
                                                    + student.getEmail()
                                    ));


            /*
             * Create notification.
             */
            notificationService.createNotification(
                    user,
                    "New Assignment",
                    "A new assignment \"" +
                            savedAssignment.getTitle() +
                            "\" has been posted in " +
                            classroom.getName(),
                    NotificationType.ASSIGNMENT,
                    savedAssignment.getId()
            );
        }


        return mapToResponse(
                savedAssignment
        );
    }


    /*
     * STUDENT
     *
     * View assignments belonging to a classroom.
     */
    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getStudentAssignments(
            Long classroomId,
            Pageable pageable) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String studentEmail =
                SecurityUtils.getCurrentUserEmail();


        /*
         * Find authenticated student.
         */
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
         * Check classroom membership.
         */
        ClassroomMembership membership =
                membershipRepository
                        .findByClassroomIdAndStudentId(
                                classroomId,
                                student.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this classroom"
                                ));


        /*
         * Student must currently be active.
         */
        if (membership.getStatus()
                != MembershipStatus.ACTIVE) {

            throw new RuntimeException(
                    "You are not an active member of this classroom"
            );
        }


        /*
         * Retrieve assignments.
         */
        return assignmentRepository
                .findByClassroomId(
                        classroomId,
                        pageable
                )
                .map(this::mapToResponse);
    }


    /*
     * FACULTY
     *
     * View assignments created in their classroom.
     */
    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getFacultyAssignments(
            Long classroomId,
            Pageable pageable) {

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
         * Find classroom.
         */
        Classroom classroom =
                classroomRepository
                        .findByIdAndInstitutionId(
                                classroomId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Classroom not found"
                                ));


        /*
         * Only classroom owner can access
         * faculty assignments.
         */
        if (!classroom
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this classroom"
            );
        }


        return assignmentRepository
                .findByClassroomId(
                        classroomId,
                        pageable
                )
                .map(this::mapToResponse);
    }


    /*
     * Convert entity to response DTO.
     */
    private AssignmentResponse mapToResponse(
            Assignment assignment) {

        Classroom classroom =
                assignment.getClassroom();

        Faculty faculty =
                assignment.getFaculty();


        return AssignmentResponse.builder()

                .id(
                        assignment.getId()
                )

                .classroomId(
                        classroom.getId()
                )

                .classroomName(
                        classroom.getName()
                )

                .facultyId(
                        faculty.getId()
                )

                .facultyName(
                        faculty.getFirstName()
                                + " "
                                + faculty.getLastName()
                )

                .title(
                        assignment.getTitle()
                )

                .description(
                        assignment.getDescription()
                )

                .dueDate(
                        assignment.getDueDate()
                )

                .maxMarks(
                        assignment.getMaxMarks()
                )

                .fileId(
                        assignment.getFile() != null
                                ? assignment.getFile().getId()
                                : null
                )

                .fileName(
                        assignment.getFile() != null
                                ? assignment.getFile().getOriginalName()
                                : null
                )

                .createdAt(
                        assignment.getCreatedAt()
                )

                .updatedAt(
                        assignment.getUpdatedAt()
                )

                .build();
    }
}