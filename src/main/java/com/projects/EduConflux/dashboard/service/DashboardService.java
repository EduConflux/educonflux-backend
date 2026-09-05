package com.projects.EduConflux.dashboard.service;

import com.projects.EduConflux.assignment.entity.Assignment;
import com.projects.EduConflux.assignment.repository.AssignmentRepository;
import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.membership.entity.ClassroomMembership;
import com.projects.EduConflux.classroom.membership.entity.MembershipStatus;
import com.projects.EduConflux.classroom.membership.repository.ClassroomMembershipRepository;
import com.projects.EduConflux.classroom.repository.ClassroomRepository;
import com.projects.EduConflux.dashboard.dto.FacultyDashboardResponse;
import com.projects.EduConflux.dashboard.dto.StudentDashboardResponse;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.learning.entity.LearningMaterial;
import com.projects.EduConflux.learning.repository.LearningMaterialRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.projects.EduConflux.dashboard.dto.AdminDashboardResponse;
import com.projects.EduConflux.identity.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomMembershipRepository membershipRepository;
    private final AssignmentRepository assignmentRepository;
    private final LearningMaterialRepository learningMaterialRepository;

    public DashboardService(
            StudentRepository studentRepository,
            UserRepository userRepository,
            FacultyRepository facultyRepository,
            ClassroomRepository classroomRepository,
            ClassroomMembershipRepository membershipRepository,
            AssignmentRepository assignmentRepository,
            LearningMaterialRepository learningMaterialRepository
    ) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
        this.classroomRepository = classroomRepository;
        this.membershipRepository = membershipRepository;
        this.assignmentRepository = assignmentRepository;
        this.learningMaterialRepository = learningMaterialRepository;
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getStudentDashboard() {

        Long institutionId = SecurityUtils.getCurrentInstitutionId();
        String email = SecurityUtils.getCurrentUserEmail();

        User user = userRepository
                .findByInstitutionIdAndEmail(institutionId, email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Student student = studentRepository
                .findByInstitutionIdAndEmail(
                        institutionId,
                        user.getEmail()
                )
                .orElseThrow(() ->
                        new RuntimeException("Student profile not found")
                );

        List<ClassroomMembership> memberships =
                membershipRepository.findByStudentIdAndStatus(
                        student.getId(),
                        MembershipStatus.ACTIVE
                );

        int classroomCount = memberships.size();
        int assignmentCount = 0;
        int learningMaterialCount = 0;

        List<StudentDashboardResponse.UpcomingAssignment>
                upcomingAssignments = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (ClassroomMembership membership : memberships) {

            Long classroomId =
                    membership.getClassroom().getId();

            String classroomName =
                    membership.getClassroom().getName();

            List<Assignment> assignments =
                    assignmentRepository
                            .findByClassroomId(
                                    classroomId,
                                    PageRequest.of(0, 1000)
                            )
                            .getContent();

            assignmentCount += assignments.size();

            for (Assignment assignment : assignments) {

                if (assignment.getDueDate() != null
                        && assignment.getDueDate().isAfter(now)) {

                    upcomingAssignments.add(
                            new StudentDashboardResponse
                                    .UpcomingAssignment(
                                    assignment.getId(),
                                    classroomId,
                                    classroomName,
                                    assignment.getTitle(),
                                    assignment.getDueDate(),
                                    assignment.getMaxMarks()
                            )
                    );
                }
            }

            List<LearningMaterial> materials =
                    learningMaterialRepository
                            .findByClassroomId(
                                    classroomId,
                                    PageRequest.of(0, 1000)
                            )
                            .getContent();

            learningMaterialCount += materials.size();
        }

        upcomingAssignments.sort(
                Comparator.comparing(
                        StudentDashboardResponse
                                .UpcomingAssignment::getDueDate
                )
        );

        return new StudentDashboardResponse(
                student.getId(),
                student.getFirstName()
                        + " "
                        + student.getLastName(),
                student.getEmail(),
                student.getEnrollmentNumber(),
                classroomCount,
                assignmentCount,
                learningMaterialCount,
                upcomingAssignments
        );
    }

    @Transactional(readOnly = true)
    public FacultyDashboardResponse getFacultyDashboard() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String email =
                SecurityUtils.getCurrentUserEmail();

        User user = userRepository
                .findByInstitutionIdAndEmail(
                        institutionId,
                        email
                )
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Faculty faculty = facultyRepository
                .findByInstitutionIdAndEmail(
                        institutionId,
                        user.getEmail()
                )
                .orElseThrow(() ->
                        new RuntimeException("Faculty profile not found")
                );

        List<Classroom> classrooms =
                classroomRepository
                        .findByFacultyIdAndInstitutionId(
                                faculty.getId(),
                                institutionId
                        );

        int classroomCount = classrooms.size();
        int studentCount = 0;
        int assignmentCount = 0;
        int learningMaterialCount = 0;

        Set<Long> studentIds = new HashSet<>();

        List<FacultyDashboardResponse.UpcomingAssignment>
                upcomingAssignments = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (Classroom classroom : classrooms) {

            List<ClassroomMembership> memberships =
                    membershipRepository
                            .findByClassroomIdAndStatus(
                                    classroom.getId(),
                                    MembershipStatus.ACTIVE
                            );

            for (ClassroomMembership membership : memberships) {

                if (membership.getStudent() != null) {
                    studentIds.add(
                            membership.getStudent().getId()
                    );
                }
            }

            List<Assignment> assignments =
                    assignmentRepository
                            .findByClassroomId(
                                    classroom.getId(),
                                    PageRequest.of(0, 1000)
                            )
                            .getContent();

            assignmentCount += assignments.size();

            for (Assignment assignment : assignments) {

                if (assignment.getDueDate() != null
                        && assignment.getDueDate().isAfter(now)) {

                    upcomingAssignments.add(
                            new FacultyDashboardResponse
                                    .UpcomingAssignment(
                                    assignment.getId(),
                                    classroom.getId(),
                                    classroom.getName(),
                                    assignment.getTitle(),
                                    assignment.getDueDate(),
                                    assignment.getMaxMarks()
                            )
                    );
                }
            }

            List<LearningMaterial> materials =
                    learningMaterialRepository
                            .findByClassroomId(
                                    classroom.getId(),
                                    PageRequest.of(0, 1000)
                            )
                            .getContent();

            learningMaterialCount += materials.size();
        }

        studentCount = studentIds.size();

        upcomingAssignments.sort(
                Comparator.comparing(
                        FacultyDashboardResponse
                                .UpcomingAssignment::getDueDate
                )
        );

        return new FacultyDashboardResponse(
                faculty.getId(),
                faculty.getFirstName()
                        + " "
                        + faculty.getLastName(),
                faculty.getEmail(),
                classroomCount,
                studentCount,
                assignmentCount,
                learningMaterialCount,
                upcomingAssignments
        );
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        User user = userRepository
                .findByIdAndInstitutionId(
                        SecurityUtils.getCurrentUserId(),
                        institutionId
                )
                .orElseThrow(() ->
                        new RuntimeException("Admin user not found")
                );

        long totalUsers =
                userRepository.countByInstitutionId(
                        institutionId
                );

        long totalStudents =
                studentRepository.findByInstitutionId(
                        institutionId
                ).size();

        long totalFaculty =
                facultyRepository.findByInstitutionId(
                        institutionId
                ).size();

        long totalClassrooms =
                classroomRepository.findByInstitutionId(
                        institutionId
                ).size();

        long totalAssignments = 0;

        long totalLearningMaterials = 0;

        List<Classroom> classrooms =
                classroomRepository.findByInstitutionId(
                        institutionId
                );

        for (Classroom classroom : classrooms) {

            totalAssignments +=
                    assignmentRepository
                            .findByClassroomId(
                                    classroom.getId(),
                                    PageRequest.of(0, 1000)
                            )
                            .getContent()
                            .size();

            totalLearningMaterials +=
                    learningMaterialRepository
                            .findByClassroomId(
                                    classroom.getId(),
                                    PageRequest.of(0, 1000)
                            )
                            .getContent()
                            .size();
        }

        String institutionName =
                user.getInstitution().getName();

        return new AdminDashboardResponse(
                institutionId,
                institutionName,
                totalUsers,
                totalStudents,
                totalFaculty,
                totalClassrooms,
                totalAssignments,
                totalLearningMaterials
        );
    }
}