package com.projects.EduConflux.curriculum.service;

import com.projects.EduConflux.academic.entity.AcademicYear;
import com.projects.EduConflux.academic.entity.Course;
import com.projects.EduConflux.academic.repository.AcademicYearRepository;
import com.projects.EduConflux.academic.repository.CourseRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.curriculum.dto.CourseOfferingRequest;
import com.projects.EduConflux.curriculum.dto.CourseOfferingResponse;
import com.projects.EduConflux.curriculum.entity.CourseOffering;
import com.projects.EduConflux.curriculum.entity.CourseOfferingStatus;
import com.projects.EduConflux.curriculum.repository.CourseOfferingRepository;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.institution.repository.InstitutionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseOfferingService {

    private final CourseOfferingRepository courseOfferingRepository;

    private final CourseRepository courseRepository;

    private final AcademicYearRepository academicYearRepository;

    private final InstitutionRepository institutionRepository;


    public List<CourseOfferingResponse> getAllOfferings() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return courseOfferingRepository
                .findByInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public CourseOfferingResponse getOfferingById(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        CourseOffering offering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found or does not belong to your institution"
                                ));

        return toResponse(offering);
    }


    public List<CourseOfferingResponse> getOfferingsByAcademicYear(
            Long academicYearId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return courseOfferingRepository
                .findByInstitutionIdAndAcademicYearId(
                        institutionId,
                        academicYearId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public List<CourseOfferingResponse> getOfferingsByStatus(
            CourseOfferingStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return courseOfferingRepository
                .findByInstitutionIdAndStatus(
                        institutionId,
                        status
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @Transactional
    public CourseOfferingResponse createOffering(
            CourseOfferingRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        Institution institution =
                institutionRepository
                        .findById(institutionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Institution not found"
                                ));


        Course course =
                courseRepository
                        .findById(request.getCourseId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found"
                                ));


        /*
         * Verify that the course belongs
         * to the current institution.
         */
        if (!course.getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Course does not belong to your institution"
            );
        }


        AcademicYear academicYear =
                academicYearRepository
                        .findById(
                                request.getAcademicYearId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Academic year not found"
                                ));


        /*
         * Verify that the academic year belongs
         * to the current institution.
         */
        if (!academicYear
                .getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Academic year does not belong to your institution"
            );
        }


        /*
         * Prevent the same course from being
         * offered twice in the same academic year.
         */
        if (courseOfferingRepository
                .existsByInstitutionIdAndCourseIdAndAcademicYearId(
                        institutionId,
                        request.getCourseId(),
                        request.getAcademicYearId()
                )) {

            throw new RuntimeException(
                    "This course is already offered for this academic year"
            );
        }


        CourseOffering offering =
                CourseOffering.builder()
                        .institution(institution)
                        .course(course)
                        .academicYear(academicYear)
                        .status(
                                CourseOfferingStatus.PLANNED
                        )
                        .build();


        CourseOffering savedOffering =
                courseOfferingRepository.save(
                        offering
                );


        return toResponse(savedOffering);
    }


    @Transactional
    public CourseOfferingResponse updateOffering(
            Long id,
            CourseOfferingRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        CourseOffering offering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found or does not belong to your institution"
                                ));


        Course course =
                courseRepository
                        .findById(request.getCourseId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found"
                                ));


        if (!course.getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Course does not belong to your institution"
            );
        }


        AcademicYear academicYear =
                academicYearRepository
                        .findById(
                                request.getAcademicYearId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Academic year not found"
                                ));


        if (!academicYear
                .getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Academic year does not belong to your institution"
            );
        }


        /*
         * Only check duplicate if the course/year
         * combination is actually being changed.
         */
        boolean changed =
                !offering.getCourse()
                        .getId()
                        .equals(request.getCourseId())
                        ||
                        !offering.getAcademicYear()
                                .getId()
                                .equals(request.getAcademicYearId());


        if (changed &&
                courseOfferingRepository
                        .existsByInstitutionIdAndCourseIdAndAcademicYearId(
                                institutionId,
                                request.getCourseId(),
                                request.getAcademicYearId()
                        )) {

            throw new RuntimeException(
                    "This course is already offered for this academic year"
            );
        }


        offering.setCourse(course);
        offering.setAcademicYear(academicYear);


        CourseOffering updatedOffering =
                courseOfferingRepository.save(
                        offering
                );


        return toResponse(updatedOffering);
    }


    public CourseOfferingResponse updateStatus(
            Long id,
            CourseOfferingStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        CourseOffering offering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found or does not belong to your institution"
                                ));


        offering.setStatus(status);


        return toResponse(
                courseOfferingRepository.save(
                        offering
                )
        );
    }


    public void deleteOffering(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        CourseOffering offering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found or does not belong to your institution"
                                ));


        if (offering.getStatus()
                == CourseOfferingStatus.ACTIVE) {

            throw new RuntimeException(
                    "Active course offering cannot be deleted"
            );
        }


        courseOfferingRepository.delete(
                offering
        );
    }


    private CourseOfferingResponse toResponse(
            CourseOffering offering) {

        CourseOfferingResponse response =
                new CourseOfferingResponse();


        response.setId(
                offering.getId()
        );


        response.setInstitutionId(
                offering.getInstitution().getId()
        );


        Course course =
                offering.getCourse();


        response.setCourseId(
                course.getId()
        );

        response.setCourseCode(
                course.getCode()
        );

        response.setCourseName(
                course.getName()
        );


        /*
         * Program comes from Course.
         */
        response.setProgramId(
                course.getProgram().getId()
        );

        response.setProgramName(
                course.getProgram().getName()
        );


        /*
         * Semester comes from Course.
         */
        response.setSemesterId(
                course.getSemester().getId()
        );

        response.setSemesterNumber(
                course.getSemester()
                        .getSemesterNumber()
        );

        response.setSemesterName(
                course.getSemester()
                        .getName()
        );


        AcademicYear academicYear =
                offering.getAcademicYear();


        response.setAcademicYearId(
                academicYear.getId()
        );

        response.setAcademicYearName(
                academicYear.getName()
        );


        response.setStatus(
                offering.getStatus()
        );


        response.setCreatedAt(
                offering.getCreatedAt()
        );

        response.setUpdatedAt(
                offering.getUpdatedAt()
        );


        return response;
    }
}