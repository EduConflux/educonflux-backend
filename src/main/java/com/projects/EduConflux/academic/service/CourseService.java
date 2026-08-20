package com.projects.EduConflux.academic.service;

import com.projects.EduConflux.academic.dto.CourseRequest;
import com.projects.EduConflux.academic.dto.CourseResponse;
import com.projects.EduConflux.academic.entity.Course;
import com.projects.EduConflux.academic.entity.CourseStatus;
import com.projects.EduConflux.academic.entity.Department;
import com.projects.EduConflux.academic.entity.Program;
import com.projects.EduConflux.academic.entity.ProgramStatus;
import com.projects.EduConflux.academic.entity.Semester;
import com.projects.EduConflux.academic.entity.SemesterStatus;
import com.projects.EduConflux.academic.repository.CourseRepository;
import com.projects.EduConflux.academic.repository.DepartmentRepository;
import com.projects.EduConflux.academic.repository.ProgramRepository;
import com.projects.EduConflux.academic.repository.SemesterRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.institution.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final SemesterRepository semesterRepository;
    private final InstitutionRepository institutionRepository;

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return courseRepository
                .findByInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByProgram(
            Long programId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        programRepository
                .findByIdAndInstitutionId(
                        programId,
                        institutionId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Program not found or does not belong to your institution"
                        ));

        return courseRepository
                .findByProgramIdAndInstitutionId(
                        programId,
                        institutionId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesBySemester(
            Long semesterId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        semesterRepository
                .findByIdAndInstitutionId(
                        semesterId,
                        institutionId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Semester not found or does not belong to your institution"
                        ));

        return courseRepository
                .findBySemesterIdAndInstitutionId(
                        semesterId,
                        institutionId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Course course =
                courseRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found"
                                ));

        return toResponse(course);
    }

    @Transactional
    public CourseResponse createCourse(
            CourseRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Institution institution =
                institutionRepository
                        .findById(institutionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Institution not found"
                                ));

        Department department =
                departmentRepository
                        .findByIdAndInstitutionId(
                                request.getDepartmentId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found or does not belong to your institution"
                                ));

        Program program =
                programRepository
                        .findByIdAndInstitutionId(
                                request.getProgramId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Program not found or does not belong to your institution"
                                ));

        Semester semester =
                semesterRepository
                        .findByIdAndInstitutionId(
                                request.getSemesterId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Semester not found or does not belong to your institution"
                                ));

        validateHierarchy(
                department,
                program,
                semester
        );

        if (program.getStatus() != ProgramStatus.ACTIVE) {

            throw new RuntimeException(
                    "Cannot create course for an inactive program"
            );
        }

        if (semester.getStatus() != SemesterStatus.ACTIVE) {

            throw new RuntimeException(
                    "Cannot create course for an inactive or completed semester"
            );
        }

        if (courseRepository
                .existsByInstitutionIdAndCode(
                        institutionId,
                        request.getCode()
                )) {

            throw new RuntimeException(
                    "Course with this code already exists"
            );
        }

        Course course =
                Course.builder()
                        .institution(institution)
                        .department(department)
                        .program(program)
                        .semester(semester)
                        .code(request.getCode())
                        .name(request.getName())
                        .description(request.getDescription())
                        .credits(request.getCredits())
                        .courseType(request.getCourseType())
                        .status(CourseStatus.ACTIVE)
                        .build();

        Course savedCourse =
                courseRepository.save(course);

        return toResponse(savedCourse);
    }

    @Transactional
    public CourseResponse updateCourse(
            Long id,
            CourseRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Course course =
                courseRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found"
                                ));

        Department department =
                departmentRepository
                        .findByIdAndInstitutionId(
                                request.getDepartmentId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found or does not belong to your institution"
                                ));

        Program program =
                programRepository
                        .findByIdAndInstitutionId(
                                request.getProgramId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Program not found or does not belong to your institution"
                                ));

        Semester semester =
                semesterRepository
                        .findByIdAndInstitutionId(
                                request.getSemesterId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Semester not found or does not belong to your institution"
                                ));

        validateHierarchy(
                department,
                program,
                semester
        );

        boolean codeChanged =
                !course.getCode()
                        .equals(request.getCode());

        if (codeChanged &&
                courseRepository
                        .existsByInstitutionIdAndCode(
                                institutionId,
                                request.getCode()
                        )) {

            throw new RuntimeException(
                    "Course with this code already exists"
            );
        }

        course.setDepartment(department);
        course.setProgram(program);
        course.setSemester(semester);
        course.setCode(request.getCode());
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCredits(request.getCredits());
        course.setCourseType(request.getCourseType());

        Course updatedCourse =
                courseRepository.save(course);

        return toResponse(updatedCourse);
    }

    @Transactional
    public CourseResponse updateCourseStatus(
            Long id,
            CourseStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Course course =
                courseRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found"
                                ));

        course.setStatus(status);

        Course updatedCourse =
                courseRepository.save(course);

        return toResponse(updatedCourse);
    }

    @Transactional
    public void deleteCourse(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Course course =
                courseRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course not found"
                                ));

        if (course.getStatus()
                == CourseStatus.ACTIVE) {

            throw new RuntimeException(
                    "Active course cannot be deleted"
            );
        }

        courseRepository.delete(course);
    }

    private void validateHierarchy(
            Department department,
            Program program,
            Semester semester) {

        if (!program.getDepartment()
                .getId()
                .equals(department.getId())) {

            throw new RuntimeException(
                    "Program does not belong to the selected department"
            );
        }

        if (!semester.getProgram()
                .getId()
                .equals(program.getId())) {

            throw new RuntimeException(
                    "Semester does not belong to the selected program"
            );
        }
    }

    private CourseResponse toResponse(
            Course course) {

        CourseResponse response =
                new CourseResponse();

        response.setId(
                course.getId()
        );

        response.setInstitutionId(
                course.getInstitution()
                        .getId()
        );

        response.setDepartmentId(
                course.getDepartment()
                        .getId()
        );

        response.setDepartmentName(
                course.getDepartment()
                        .getName()
        );

        response.setProgramId(
                course.getProgram()
                        .getId()
        );

        response.setProgramName(
                course.getProgram()
                        .getName()
        );

        response.setSemesterId(
                course.getSemester()
                        .getId()
        );

        response.setSemesterName(
                course.getSemester()
                        .getName()
        );

        response.setSemesterNumber(
                course.getSemester()
                        .getSemesterNumber()
        );

        response.setCode(
                course.getCode()
        );

        response.setName(
                course.getName()
        );

        response.setDescription(
                course.getDescription()
        );

        response.setCredits(
                course.getCredits()
        );

        response.setCourseType(
                course.getCourseType()
        );

        response.setStatus(
                course.getStatus()
        );

        response.setCreatedAt(
                course.getCreatedAt()
        );

        response.setUpdatedAt(
                course.getUpdatedAt()
        );

        return response;
    }
}