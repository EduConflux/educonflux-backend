package com.projects.EduConflux.classroom.service;

import com.projects.EduConflux.academic.entity.ClassSection;
import com.projects.EduConflux.academic.repository.ClassSectionRepository;
import com.projects.EduConflux.classroom.dto.ClassroomRequest;
import com.projects.EduConflux.classroom.dto.ClassroomResponse;
import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.entity.ClassroomStatus;
import com.projects.EduConflux.classroom.repository.ClassroomRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.curriculum.entity.CourseOffering;
import com.projects.EduConflux.curriculum.entity.FacultyCourseAssignment;
import com.projects.EduConflux.curriculum.repository.CourseOfferingRepository;
import com.projects.EduConflux.curriculum.repository.FacultyCourseAssignmentRepository;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final FacultyRepository facultyRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final ClassSectionRepository classSectionRepository;
    private final FacultyCourseAssignmentRepository facultyCourseAssignmentRepository;


    @Transactional
    public ClassroomResponse createClassroom(
            ClassroomRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String facultyEmail =
                SecurityUtils.getCurrentUserEmail();


        /*
         * Find the faculty profile from
         * the authenticated user's email.
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
         * Find course offering.
         */
        CourseOffering courseOffering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                request.getCourseOfferingId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found"
                                ));


        /*
         * Find class section.
         */
        ClassSection classSection =
                classSectionRepository
                        .findByIdAndInstitutionId(
                                request.getClassSectionId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Class section not found"
                                ));


        /*
         * Make sure the faculty is actually
         * assigned to this course offering.
         */
        boolean assigned =
                facultyCourseAssignmentRepository
                        .existsByFacultyIdAndCourseOfferingId(
                                faculty.getId(),
                                courseOffering.getId()
                        );

        if (!assigned) {

            throw new RuntimeException(
                    "You are not assigned to this course offering"
            );
        }


        /*
         * Make sure the class section belongs
         * to the same semester as the course.
         */
        if (!classSection
                .getSemester()
                .getId()
                .equals(
                        courseOffering
                                .getCourse()
                                .getSemester()
                                .getId()
                )) {

            throw new RuntimeException(
                    "Class section does not belong to the course semester"
            );
        }


        /*
         * Prevent duplicate classroom names
         * for the same faculty.
         */
        if (classroomRepository
                .existsByFacultyIdAndName(
                        faculty.getId(),
                        request.getName()
                )) {

            throw new RuntimeException(
                    "You already have a classroom with this name"
            );
        }


        Classroom classroom =
                Classroom.builder()
                        .institution(
                                courseOffering.getInstitution()
                        )
                        .faculty(faculty)
                        .courseOffering(courseOffering)
                        .classSection(classSection)
                        .name(request.getName())
                        .description(request.getDescription())
                        .status(ClassroomStatus.ACTIVE)
                        .build();


        classroom =
                classroomRepository.save(
                        classroom
                );


        return mapToResponse(classroom);
    }


    @Transactional(readOnly = true)
    public List<ClassroomResponse> getMyClassrooms() {

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


        return classroomRepository
                .findByFacultyIdAndInstitutionId(
                        faculty.getId(),
                        institutionId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public ClassroomResponse getClassroomById(
            Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Classroom classroom =
                classroomRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Classroom not found"
                                ));

        return mapToResponse(classroom);
    }


    @Transactional
    public ClassroomResponse updateClassroom(
            Long id,
            ClassroomRequest request) {

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
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Classroom not found"
                                ));


        /*
         * Only the owner faculty can update
         * the classroom.
         */
        if (!classroom
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this classroom"
            );
        }


        CourseOffering courseOffering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                request.getCourseOfferingId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found"
                                ));


        ClassSection classSection =
                classSectionRepository
                        .findByIdAndInstitutionId(
                                request.getClassSectionId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Class section not found"
                                ));


        boolean assigned =
                facultyCourseAssignmentRepository
                        .existsByFacultyIdAndCourseOfferingId(
                                faculty.getId(),
                                courseOffering.getId()
                        );

        if (!assigned) {

            throw new RuntimeException(
                    "You are not assigned to this course offering"
            );
        }


        classroom.setName(request.getName());
        classroom.setDescription(
                request.getDescription()
        );
        classroom.setCourseOffering(
                courseOffering
        );
        classroom.setClassSection(
                classSection
        );


        return mapToResponse(
                classroomRepository.save(classroom)
        );
    }


    @Transactional
    public void archiveClassroom(Long id) {

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
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Classroom not found"
                                ));


        if (!classroom
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not the owner of this classroom"
            );
        }


        classroom.setStatus(
                ClassroomStatus.ARCHIVED
        );

        classroomRepository.save(classroom);
    }


    private ClassroomResponse mapToResponse(
            Classroom classroom) {

        return ClassroomResponse.builder()

                .id(classroom.getId())

                .institutionId(
                        classroom
                                .getInstitution()
                                .getId()
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

                .courseOfferingId(
                        classroom
                                .getCourseOffering()
                                .getId()
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

                .name(classroom.getName())

                .description(
                        classroom.getDescription()
                )

                .status(
                        classroom.getStatus()
                )

                .createdAt(
                        classroom.getCreatedAt()
                )

                .updatedAt(
                        classroom.getUpdatedAt()
                )

                .build();
    }
}