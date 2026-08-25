package com.projects.EduConflux.classroom.post.service;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.post.dto.CreatePostRequest;
import com.projects.EduConflux.classroom.post.dto.PostResponse;
import com.projects.EduConflux.classroom.post.entity.ClassroomPost;
import com.projects.EduConflux.classroom.post.repository.ClassroomPostRepository;
import com.projects.EduConflux.classroom.membership.entity.ClassroomMembership;
import com.projects.EduConflux.classroom.membership.entity.MembershipStatus;
import com.projects.EduConflux.classroom.membership.repository.ClassroomMembershipRepository;
import com.projects.EduConflux.classroom.repository.ClassroomRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClassroomPostService {

    private final ClassroomPostRepository postRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomMembershipRepository membershipRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;


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


        ClassroomPost post =
                ClassroomPost.builder()
                        .classroom(classroom)
                        .faculty(faculty)
                        .type(request.getType())
                        .title(request.getTitle())
                        .content(request.getContent())
                        .build();


        ClassroomPost savedPost =
                postRepository.save(post);


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
         * Now retrieve the posts.
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