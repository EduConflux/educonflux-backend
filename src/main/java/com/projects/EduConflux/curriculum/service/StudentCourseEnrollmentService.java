package com.projects.EduConflux.curriculum.service;

import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.curriculum.dto.StudentCourseEnrollmentRequest;
import com.projects.EduConflux.curriculum.dto.StudentCourseEnrollmentResponse;
import com.projects.EduConflux.curriculum.entity.CourseOffering;
import com.projects.EduConflux.curriculum.entity.CourseOfferingStatus;
import com.projects.EduConflux.curriculum.entity.StudentCourseEnrollment;
import com.projects.EduConflux.curriculum.entity.StudentCourseEnrollmentStatus;
import com.projects.EduConflux.curriculum.repository.CourseOfferingRepository;
import com.projects.EduConflux.curriculum.repository.StudentCourseEnrollmentRepository;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.entity.StudentStatus;
import com.projects.EduConflux.student.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentCourseEnrollmentService {

    private final StudentCourseEnrollmentRepository enrollmentRepository;

    private final StudentRepository studentRepository;

    private final CourseOfferingRepository courseOfferingRepository;


    public List<StudentCourseEnrollmentResponse> getAllEnrollments() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return enrollmentRepository
                .findByStudentInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public StudentCourseEnrollmentResponse getEnrollmentById(
            Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        StudentCourseEnrollment enrollment =
                enrollmentRepository
                        .findByIdAndStudentInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student course enrollment not found or does not belong to your institution"
                                ));

        return toResponse(enrollment);
    }


    public List<StudentCourseEnrollmentResponse>
    getEnrollmentsByStudent(Long studentId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Student student =
                studentRepository
                        .findByIdAndInstitutionId(
                                studentId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found or does not belong to your institution"
                                ));

        return enrollmentRepository
                .findByStudentIdAndStatus(
                        student.getId(),
                        StudentCourseEnrollmentStatus.ENROLLED
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public List<StudentCourseEnrollmentResponse>
    getEnrollmentsByCourseOffering(
            Long courseOfferingId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        CourseOffering courseOffering =
                courseOfferingRepository
                        .findByIdAndInstitutionId(
                                courseOfferingId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Course offering not found or does not belong to your institution"
                                ));

        return enrollmentRepository
                .findByCourseOfferingIdAndStatus(
                        courseOffering.getId(),
                        StudentCourseEnrollmentStatus.ENROLLED
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @Transactional
    public StudentCourseEnrollmentResponse createEnrollment(
            StudentCourseEnrollmentRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        /*
         * Find the student and verify institution.
         */
        Student student =
                studentRepository
                        .findByIdAndInstitutionId(
                                request.getStudentId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found or does not belong to your institution"
                                ));


        /*
         * Find the course offering and verify institution.
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
         * Student must be active.
         */
        if (student.getStatus() != StudentStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active students can be enrolled in courses"
            );
        }


        /*
         * Course offering must be active.
         */
        if (courseOffering.getStatus()
                != CourseOfferingStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active course offerings can have students enrolled"
            );
        }


        /*
         * Student's program must match
         * the course's program.
         */
        if (!student.getProgram()
                .getId()
                .equals(
                        courseOffering
                                .getCourse()
                                .getProgram()
                                .getId()
                )) {

            throw new RuntimeException(
                    "Student's program does not match the course program"
            );
        }


        /*
         * Student's academic year must match
         * the course offering's academic year.
         */
        if (!student.getAcademicYear()
                .getId()
                .equals(
                        courseOffering
                                .getAcademicYear()
                                .getId()
                )) {

            throw new RuntimeException(
                    "Student's academic year does not match the course offering"
            );
        }


        /*
         * Prevent duplicate enrollment.
         */
        if (enrollmentRepository
                .existsByStudentIdAndCourseOfferingId(
                        student.getId(),
                        courseOffering.getId()
                )) {

            throw new RuntimeException(
                    "Student is already enrolled in this course offering"
            );
        }


        StudentCourseEnrollment enrollment =
                StudentCourseEnrollment.builder()
                        .student(student)
                        .courseOffering(courseOffering)
                        .status(
                                StudentCourseEnrollmentStatus.ENROLLED
                        )
                        .build();


        StudentCourseEnrollment savedEnrollment =
                enrollmentRepository.save(
                        enrollment
                );


        return toResponse(savedEnrollment);
    }


    @Transactional
    public StudentCourseEnrollmentResponse updateEnrollment(
            Long id,
            StudentCourseEnrollmentRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        StudentCourseEnrollment enrollment =
                enrollmentRepository
                        .findByIdAndStudentInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student course enrollment not found or does not belong to your institution"
                                ));


        Student student =
                studentRepository
                        .findByIdAndInstitutionId(
                                request.getStudentId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found or does not belong to your institution"
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


        if (student.getStatus() != StudentStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active students can be enrolled in courses"
            );
        }


        if (courseOffering.getStatus()
                != CourseOfferingStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active course offerings can have students enrolled"
            );
        }


        if (!student.getProgram()
                .getId()
                .equals(
                        courseOffering
                                .getCourse()
                                .getProgram()
                                .getId()
                )) {

            throw new RuntimeException(
                    "Student's program does not match the course program"
            );
        }


        if (!student.getAcademicYear()
                .getId()
                .equals(
                        courseOffering
                                .getAcademicYear()
                                .getId()
                )) {

            throw new RuntimeException(
                    "Student's academic year does not match the course offering"
            );
        }


        boolean changed =
                !enrollment.getStudent()
                        .getId()
                        .equals(student.getId())
                        ||
                        !enrollment.getCourseOffering()
                                .getId()
                                .equals(courseOffering.getId());


        if (changed &&
                enrollmentRepository
                        .existsByStudentIdAndCourseOfferingId(
                                student.getId(),
                                courseOffering.getId()
                        )) {

            throw new RuntimeException(
                    "Student is already enrolled in this course offering"
            );
        }


        enrollment.setStudent(student);
        enrollment.setCourseOffering(courseOffering);


        StudentCourseEnrollment updatedEnrollment =
                enrollmentRepository.save(
                        enrollment
                );


        return toResponse(updatedEnrollment);
    }


    public StudentCourseEnrollmentResponse updateStatus(
            Long id,
            StudentCourseEnrollmentStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        StudentCourseEnrollment enrollment =
                enrollmentRepository
                        .findByIdAndStudentInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student course enrollment not found or does not belong to your institution"
                                ));


        enrollment.setStatus(status);


        return toResponse(
                enrollmentRepository.save(
                        enrollment
                )
        );
    }


    public void deleteEnrollment(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();


        StudentCourseEnrollment enrollment =
                enrollmentRepository
                        .findByIdAndStudentInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student course enrollment not found or does not belong to your institution"
                                ));


        enrollmentRepository.delete(enrollment);
    }


    private StudentCourseEnrollmentResponse toResponse(
            StudentCourseEnrollment enrollment) {

        StudentCourseEnrollmentResponse response =
                new StudentCourseEnrollmentResponse();


        Student student =
                enrollment.getStudent();


        response.setId(
                enrollment.getId()
        );


        response.setStudentId(
                student.getId()
        );

        response.setEnrollmentNumber(
                student.getEnrollmentNumber()
        );

        response.setStudentName(
                student.getFirstName()
                        + " "
                        + student.getLastName()
        );

        response.setStudentEmail(
                student.getEmail()
        );


        CourseOffering courseOffering =
                enrollment.getCourseOffering();


        response.setCourseOfferingId(
                courseOffering.getId()
        );


        response.setCourseId(
                courseOffering
                        .getCourse()
                        .getId()
        );

        response.setCourseCode(
                courseOffering
                        .getCourse()
                        .getCode()
        );

        response.setCourseName(
                courseOffering
                        .getCourse()
                        .getName()
        );


        response.setProgramId(
                courseOffering
                        .getCourse()
                        .getProgram()
                        .getId()
        );

        response.setProgramName(
                courseOffering
                        .getCourse()
                        .getProgram()
                        .getName()
        );


        response.setSemesterId(
                courseOffering
                        .getCourse()
                        .getSemester()
                        .getId()
        );

        response.setSemesterNumber(
                courseOffering
                        .getCourse()
                        .getSemester()
                        .getSemesterNumber()
        );

        response.setSemesterName(
                courseOffering
                        .getCourse()
                        .getSemester()
                        .getName()
        );


        response.setAcademicYearId(
                courseOffering
                        .getAcademicYear()
                        .getId()
        );

        response.setAcademicYearName(
                courseOffering
                        .getAcademicYear()
                        .getName()
        );


        response.setStatus(
                enrollment.getStatus()
        );

        response.setCreatedAt(
                enrollment.getCreatedAt()
        );

        response.setUpdatedAt(
                enrollment.getUpdatedAt()
        );


        return response;
    }
}