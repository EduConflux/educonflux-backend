package com.projects.EduConflux.curriculum.service;

import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.curriculum.dto.FacultyCourseAssignmentRequest;
import com.projects.EduConflux.curriculum.dto.FacultyCourseAssignmentResponse;
import com.projects.EduConflux.curriculum.entity.CourseOffering;
import com.projects.EduConflux.curriculum.entity.FacultyCourseAssignment;
import com.projects.EduConflux.curriculum.entity.FacultyCourseAssignmentStatus;
import com.projects.EduConflux.curriculum.repository.CourseOfferingRepository;
import com.projects.EduConflux.curriculum.repository.FacultyCourseAssignmentRepository;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyCourseAssignmentService {

    private final FacultyCourseAssignmentRepository assignmentRepository;

    private final FacultyRepository facultyRepository;

    private final CourseOfferingRepository courseOfferingRepository;


    public List<FacultyCourseAssignmentResponse> getAllAssignments() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return assignmentRepository
                .findByFacultyInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public FacultyCourseAssignmentResponse getAssignmentById(
            Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        FacultyCourseAssignment assignment =
                assignmentRepository
                        .findByIdAndFacultyInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty course assignment not found or does not belong to your institution"
                                ));

        return toResponse(assignment);
    }


    public List<FacultyCourseAssignmentResponse>
    getAssignmentsByFaculty(Long facultyId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Faculty faculty =
                facultyRepository
                        .findByIdAndInstitutionId(
                                facultyId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty not found or does not belong to your institution"
                                ));

        return assignmentRepository
                .findByFacultyIdAndStatus(
                        faculty.getId(),
                        FacultyCourseAssignmentStatus.ACTIVE
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public List<FacultyCourseAssignmentResponse>
    getAssignmentsByCourseOffering(
            Long courseOfferingId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        CourseOffering offering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                courseOfferingId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found or does not belong to your institution"
                                ));

        return assignmentRepository
                .findByCourseOfferingIdAndStatus(
                        offering.getId(),
                        FacultyCourseAssignmentStatus.ACTIVE
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @Transactional
    public FacultyCourseAssignmentResponse createAssignment(
            FacultyCourseAssignmentRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        /*
         * Find Faculty and verify institution.
         */
        Faculty faculty =
                facultyRepository
                        .findByIdAndInstitutionId(
                                request.getFacultyId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty not found or does not belong to your institution"
                                ));


        /*
         * Find Course Offering and verify institution.
         */
        CourseOffering courseOffering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                request.getCourseOfferingId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found or does not belong to your institution"
                                ));


        /*
         * Faculty must be ACTIVE.
         */
        if (faculty.getStatus() !=
                com.projects.EduConflux.faculty.entity.FacultyStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active faculty can be assigned to courses"
            );
        }


        /*
         * Course offering must be ACTIVE.
         */
        if (courseOffering.getStatus() !=
                com.projects.EduConflux.curriculum.entity.CourseOfferingStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active course offerings can have faculty assigned"
            );
        }


        /*
         * Prevent duplicate assignment.
         */
        if (assignmentRepository
                .existsByFacultyIdAndCourseOfferingId(
                        faculty.getId(),
                        courseOffering.getId()
                )) {

            throw new RuntimeException(
                    "Faculty is already assigned to this course offering"
            );
        }


        FacultyCourseAssignment assignment =
                FacultyCourseAssignment.builder()
                        .faculty(faculty)
                        .courseOffering(courseOffering)
                        .status(
                                FacultyCourseAssignmentStatus.ACTIVE
                        )
                        .build();


        FacultyCourseAssignment savedAssignment =
                assignmentRepository.save(
                        assignment
                );


        return toResponse(savedAssignment);
    }


    @Transactional
    public FacultyCourseAssignmentResponse updateAssignment(
            Long id,
            FacultyCourseAssignmentRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        FacultyCourseAssignment assignment =
                assignmentRepository
                        .findByIdAndFacultyInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty course assignment not found or does not belong to your institution"
                                ));


        Faculty faculty =
                facultyRepository
                        .findByIdAndInstitutionId(
                                request.getFacultyId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty not found or does not belong to your institution"
                                ));


        CourseOffering courseOffering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                request.getCourseOfferingId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found or does not belong to your institution"
                                ));


        if (faculty.getStatus() !=
                com.projects.EduConflux.faculty.entity.FacultyStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active faculty can be assigned to courses"
            );
        }


        if (courseOffering.getStatus() !=
                com.projects.EduConflux.curriculum.entity.CourseOfferingStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active course offerings can have faculty assigned"
            );
        }


        boolean changed =
                !assignment.getFaculty()
                        .getId()
                        .equals(faculty.getId())
                        ||
                        !assignment.getCourseOffering()
                                .getId()
                                .equals(courseOffering.getId());


        if (changed &&
                assignmentRepository
                        .existsByFacultyIdAndCourseOfferingId(
                                faculty.getId(),
                                courseOffering.getId()
                        )) {

            throw new RuntimeException(
                    "Faculty is already assigned to this course offering"
            );
        }


        assignment.setFaculty(faculty);
        assignment.setCourseOffering(courseOffering);


        FacultyCourseAssignment updatedAssignment =
                assignmentRepository.save(
                        assignment
                );


        return toResponse(updatedAssignment);
    }


    public FacultyCourseAssignmentResponse updateStatus(
            Long id,
            FacultyCourseAssignmentStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        FacultyCourseAssignment assignment =
                assignmentRepository
                        .findByIdAndFacultyInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty course assignment not found or does not belong to your institution"
                                ));


        assignment.setStatus(status);


        return toResponse(
                assignmentRepository.save(
                        assignment
                )
        );
    }


    public void deleteAssignment(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        FacultyCourseAssignment assignment =
                assignmentRepository
                        .findByIdAndFacultyInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty course assignment not found or does not belong to your institution"
                                ));


        assignmentRepository.delete(assignment);
    }


    private FacultyCourseAssignmentResponse toResponse(
            FacultyCourseAssignment assignment) {

        FacultyCourseAssignmentResponse response =
                new FacultyCourseAssignmentResponse();


        response.setId(
                assignment.getId()
        );


        Faculty faculty =
                assignment.getFaculty();


        response.setFacultyId(
                faculty.getId()
        );

        response.setFacultyName(
                faculty.getFirstName()
                        + " "
                        + faculty.getLastName()
        );

        response.setFacultyEmployeeId(
                faculty.getEmployeeId()
        );

        response.setFacultyEmail(
                faculty.getEmail()
        );


        CourseOffering offering =
                assignment.getCourseOffering();


        response.setCourseOfferingId(
                offering.getId()
        );


        response.setCourseId(
                offering.getCourse()
                        .getId()
        );

        response.setCourseCode(
                offering.getCourse()
                        .getCode()
        );

        response.setCourseName(
                offering.getCourse()
                        .getName()
        );


        response.setProgramId(
                offering.getCourse()
                        .getProgram()
                        .getId()
        );

        response.setProgramName(
                offering.getCourse()
                        .getProgram()
                        .getName()
        );


        response.setSemesterId(
                offering.getCourse()
                        .getSemester()
                        .getId()
        );

        response.setSemesterNumber(
                offering.getCourse()
                        .getSemester()
                        .getSemesterNumber()
        );

        response.setSemesterName(
                offering.getCourse()
                        .getSemester()
                        .getName()
        );


        response.setAcademicYearId(
                offering.getAcademicYear()
                        .getId()
        );

        response.setAcademicYearName(
                offering.getAcademicYear()
                        .getName()
        );


        response.setStatus(
                assignment.getStatus()
        );

        response.setCreatedAt(
                assignment.getCreatedAt()
        );

        response.setUpdatedAt(
                assignment.getUpdatedAt()
        );


        return response;
    }
}