package com.projects.EduConflux.search.service;

import com.projects.EduConflux.assignment.entity.Assignment;
import com.projects.EduConflux.assignment.repository.AssignmentRepository;
import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.membership.entity.ClassroomMembership;
import com.projects.EduConflux.classroom.membership.entity.MembershipStatus;
import com.projects.EduConflux.classroom.membership.repository.ClassroomMembershipRepository;
import com.projects.EduConflux.classroom.repository.ClassroomRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.learning.entity.LearningMaterial;
import com.projects.EduConflux.learning.repository.LearningMaterialRepository;
import com.projects.EduConflux.search.dto.SearchResult;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomMembershipRepository membershipRepository;
    private final AssignmentRepository assignmentRepository;
    private final LearningMaterialRepository learningMaterialRepository;

    public SearchService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            ClassroomRepository classroomRepository,
            ClassroomMembershipRepository membershipRepository,
            AssignmentRepository assignmentRepository,
            LearningMaterialRepository learningMaterialRepository
    ) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.classroomRepository = classroomRepository;
        this.membershipRepository = membershipRepository;
        this.assignmentRepository = assignmentRepository;
        this.learningMaterialRepository = learningMaterialRepository;
    }

    @Transactional(readOnly = true)
    public List<SearchResult> search(String query) {

        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Search query cannot be empty"
            );
        }

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String searchQuery =
                query.trim().toLowerCase();

        List<SearchResult> results =
                new ArrayList<>();

        List<Classroom> classrooms =
                classroomRepository.findByInstitutionId(
                        institutionId
                );

        for (Classroom classroom : classrooms) {

            if (canAccessClassroom(classroom)
                    && (contains(
                    classroom.getName(),
                    searchQuery
            ))) {

                results.add(
                        new SearchResult(
                                "CLASSROOM",
                                classroom.getId(),
                                classroom.getName(),
                                "Classroom"
                        )
                );
            }
        }

        for (Classroom classroom : classrooms) {

            if (!canAccessClassroom(classroom)) {
                continue;
            }

            List<Assignment> assignments =
                    assignmentRepository
                            .findByClassroomId(
                                    classroom.getId(),
                                    PageRequest.of(0, 1000)
                            )
                            .getContent();

            for (Assignment assignment : assignments) {

                if (contains(
                        assignment.getTitle(),
                        searchQuery
                ) || contains(
                        assignment.getDescription(),
                        searchQuery
                )) {

                    results.add(
                            new SearchResult(
                                    "ASSIGNMENT",
                                    assignment.getId(),
                                    assignment.getTitle(),
                                    assignment.getDescription()
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

            for (LearningMaterial material : materials) {

                if (contains(
                        material.getTitle(),
                        searchQuery
                ) || contains(
                        material.getDescription(),
                        searchQuery
                )) {

                    results.add(
                            new SearchResult(
                                    "LEARNING_MATERIAL",
                                    material.getId(),
                                    material.getTitle(),
                                    material.getDescription()
                            )
                    );
                }
            }
        }

        return results;
    }

    private boolean canAccessClassroom(
            Classroom classroom
    ) {

        if (SecurityUtils.isInstitutionAdmin()
                || SecurityUtils.isPlatformAdmin()) {

            return true;
        }

        if (SecurityUtils.hasRole("FACULTY")) {

            String email =
                    SecurityUtils.getCurrentUserEmail();

            return classroom.getFaculty() != null
                    && classroom.getFaculty()
                    .getEmail()
                    .equalsIgnoreCase(email);
        }

        if (SecurityUtils.isStudent()) {

            Long institutionId =
                    SecurityUtils.getCurrentInstitutionId();

            String email =
                    SecurityUtils.getCurrentUserEmail();

            Student student =
                    studentRepository
                            .findByInstitutionIdAndEmail(
                                    institutionId,
                                    email
                            )
                            .orElse(null);

            if (student == null) {
                return false;
            }

            return membershipRepository
                    .findByClassroomIdAndStudentId(
                            classroom.getId(),
                            student.getId()
                    )
                    .map(membership ->
                            membership.getStatus()
                                    == MembershipStatus.ACTIVE
                    )
                    .orElse(false);
        }

        return false;
    }

    private boolean contains(
            String value,
            String query
    ) {

        return value != null
                && value.toLowerCase()
                .contains(query);
    }
}