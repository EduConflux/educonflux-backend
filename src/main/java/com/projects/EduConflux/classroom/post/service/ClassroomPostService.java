package com.projects.EduConflux.classroom.post.service;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.membership.entity.ClassroomMembership;
import com.projects.EduConflux.classroom.membership.entity.MembershipStatus;
import com.projects.EduConflux.classroom.membership.repository.ClassroomMembershipRepository;
import com.projects.EduConflux.classroom.post.dto.CreatePostRequest;
import com.projects.EduConflux.classroom.post.dto.PostResponse;
import com.projects.EduConflux.classroom.post.entity.ClassroomPost;
import com.projects.EduConflux.classroom.post.entity.PostType;
import com.projects.EduConflux.classroom.post.repository.ClassroomPostRepository;
import com.projects.EduConflux.classroom.repository.ClassroomRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
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
public class ClassroomPostService {

    private final ClassroomPostRepository postRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomMembershipRepository membershipRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    @Transactional
    public PostResponse createPost(
            Long classroomId,
            CreatePostRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String facultyEmail =
                SecurityUtils.getCurrentUserEmail();


        /*
         * Find the authenticated faculty.
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
         * Find the classroom inside the
         * authenticated user's institution.
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
         * Resource-level authorization.
         *
         * Having ROLE_FACULTY is not enough.
         * The faculty must own this classroom.
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
         * Create the classroom post.
         */
        ClassroomPost post =
                ClassroomPost.builder()
                        .classroom(classroom)
                        .faculty(faculty)
                        .type(request.getType())
                        .title(request.getTitle())
                        .content(request.getContent())
                        .build();


        /*
         * Save the post FIRST.
         *
         * We need the generated post ID for
         * the notification referenceId.
         */
        ClassroomPost savedPost =
                postRepository.save(post);


        /*
         * Only ANNOUNCEMENT posts generate
         * notifications.
         *
         * GENERAL posts do not generate
         * notifications.
         */
        if (request.getType() == PostType.ANNOUNCEMENT) {

            List<ClassroomMembership> memberships =
                    membershipRepository
                            .findByClassroomIdAndStatus(
                                    classroomId,
                                    MembershipStatus.ACTIVE
                            );


            /*
             * Create a notification for every
             * active student in the classroom.
             */
            for (ClassroomMembership membership :
                    memberships) {

                Student student =
                        membership.getStudent();


                /*
                 * Find the corresponding User
                 * account of the student.
                 */
                User user =
                        userRepository
                                .findByInstitutionIdAndEmail(
                                        institutionId,
                                        student.getEmail()
                                )
                                .orElse(null);


                /*
                 * If the User account exists,
                 * create the notification.
                 */
                if (user != null) {

                    notificationService.createNotification(
                            user,
                            "New Classroom Announcement",
                            savedPost.getTitle(),
                            NotificationType.CLASSROOM_ANNOUNCEMENT,
                            savedPost.getId()
                    );
                }
            }
        }


        /*
         * Return the saved post.
         */
        return mapToResponse(savedPost);
    }


    @Transactional(readOnly = true)
    public Page<PostResponse> getStudentPosts(
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
         * Check membership.
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
         * Student must currently be ACTIVE.
         */
        if (membership.getStatus()
                != MembershipStatus.ACTIVE) {

            throw new RuntimeException(
                    "You are not an active member of this classroom"
            );
        }


        /*
         * Retrieve the posts.
         */
        return postRepository
                .findByClassroomId(
                        classroomId,
                        pageable
                )
                .map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public Page<PostResponse> getFacultyPosts(
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
         * Find the classroom.
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
         * faculty-side posts.
         */
        if (!classroom
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this classroom"
            );
        }


        return postRepository
                .findByClassroomId(
                        classroomId,
                        pageable
                )
                .map(this::mapToResponse);
    }


    /*
     * Convert entity to response DTO.
     */
    private PostResponse mapToResponse(
            ClassroomPost post) {

        Faculty faculty =
                post.getFaculty();

        Classroom classroom =
                post.getClassroom();


        return PostResponse.builder()

                .id(post.getId())

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

                .type(
                        post.getType()
                )

                .title(
                        post.getTitle()
                )

                .content(
                        post.getContent()
                )

                .createdAt(
                        post.getCreatedAt()
                )

                .updatedAt(
                        post.getUpdatedAt()
                )

                .build();
    }
}