package com.projects.EduConflux.classroom.membership.service;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.membership.dto.MembershipResponse;
import com.projects.EduConflux.classroom.membership.dto.StudentClassroomResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomMembershipService {

    private final ClassroomMembershipRepository membershipRepository;
    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final FacultyRepository facultyRepository;


    @Transactional(readOnly = true)
    public List<StudentClassroomResponse> getMyClassrooms() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String studentEmail =
                SecurityUtils.getCurrentUserEmail();


        /*
         * Get the student from the authenticated JWT.
         *
         * We do NOT accept studentId from the request.
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
         * Get all active classroom memberships
         * belonging to this student.
         */
        List<ClassroomMembership> memberships =
                membershipRepository
                        .findByStudentIdAndStatus(
                                student.getId(),
                                MembershipStatus.ACTIVE
                        );


        /*
         * Convert memberships into classroom responses.
         */
        return memberships
                .stream()
                .map(this::mapToStudentClassroomResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> getClassroomMembers(
            Long classroomId) {

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
         * Only the faculty who owns the classroom
         * can see its members.
         */
        if (!classroom
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this classroom"
            );
        }


        return membershipRepository
                .findByClassroomIdAndStatus(
                        classroomId,
                        MembershipStatus.ACTIVE
                )
                .stream()
                .map(this::mapToMembershipResponse)
                .toList();
    }

    @Transactional
    public void removeStudent(
            Long classroomId,
            Long studentId) {

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
         * Authorization:
         * only classroom owner can remove students.
         */
        if (!classroom
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this classroom"
            );
        }


        ClassroomMembership membership =
                membershipRepository
                        .findByClassroomIdAndStudentId(
                                classroomId,
                                studentId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student is not a member of this classroom"
                                ));


        if (membership.getStatus()
                == MembershipStatus.REMOVED) {

            throw new RuntimeException(
                    "Student is already removed"
            );
        }


        /*
         * Soft removal.
         */
        membership.setStatus(
                MembershipStatus.REMOVED
        );

        membership.setRemovedAt(
                java.time.LocalDateTime.now()
        );

        membershipRepository.save(
                membership
        );
    }

    private MembershipResponse mapToMembershipResponse(
            ClassroomMembership membership) {

        Student student = membership.getStudent();

        Classroom classroom = membership.getClassroom();

        return MembershipResponse.builder()

                .membershipId(
                        membership.getId()
                )

                .classroomId(
                        classroom.getId()
                )

                .classroomName(
                        classroom.getName()
                )

                .studentId(
                        student.getId()
                )

                .studentName(
                        student.getFirstName()
                                + " "
                                + student.getLastName()
                )

                .enrollmentNumber(
                        student.getEnrollmentNumber()
                )

                .email(
                        student.getEmail()
                )

                .status(
                        membership.getStatus()
                )

                .joinedAt(
                        membership.getJoinedAt()
                )

                .removedAt(
                        membership.getRemovedAt()
                )

                .build();
    }


    private StudentClassroomResponse
    mapToStudentClassroomResponse(
            ClassroomMembership membership) {

        Classroom classroom =
                membership.getClassroom();


        return StudentClassroomResponse
                .builder()

                .classroomId(
                        classroom.getId()
                )

                .classroomName(
                        classroom.getName()
                )

                .description(
                        classroom.getDescription()
                )

                .facultyId(
                        classroom
                                .getFaculty()
                                .getId()
                )

                .facultyName(
                        classroom
                                .getFaculty()
                                .getFirstName()
                                + " "
                                + classroom
                                .getFaculty()
                                .getLastName()
                )

                .courseId(
                        classroom
                                .getCourseOffering()
                                .getCourse()
                                .getId()
                )

                .courseCode(
                        classroom
                                .getCourseOffering()
                                .getCourse()
                                .getCode()
                )

                .courseName(
                        classroom
                                .getCourseOffering()
                                .getCourse()
                                .getName()
                )

                .classSectionId(
                        classroom
                                .getClassSection()
                                .getId()
                )

                .classSectionName(
                        classroom
                                .getClassSection()
                                .getName()
                )

                .status(
                        classroom.getStatus()
                )

                .build();
    }
}