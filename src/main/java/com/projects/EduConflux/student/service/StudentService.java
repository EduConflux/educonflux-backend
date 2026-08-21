package com.projects.EduConflux.student.service;

import com.projects.EduConflux.academic.entity.AcademicYear;
import com.projects.EduConflux.academic.entity.ClassSection;
import com.projects.EduConflux.academic.entity.Program;
import com.projects.EduConflux.academic.repository.AcademicYearRepository;
import com.projects.EduConflux.academic.repository.ClassSectionRepository;
import com.projects.EduConflux.academic.repository.ProgramRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.identity.dto.UserRequest;
import com.projects.EduConflux.identity.service.UserService;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.institution.repository.InstitutionRepository;
import com.projects.EduConflux.student.dto.StudentRequest;
import com.projects.EduConflux.student.dto.StudentResponse;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.entity.StudentStatus;
import com.projects.EduConflux.student.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    private final InstitutionRepository institutionRepository;

    private final ProgramRepository programRepository;

    private final AcademicYearRepository academicYearRepository;

    private final ClassSectionRepository classSectionRepository;

    private final UserService userService;

    public List<StudentResponse> getAllStudents() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return studentRepository
                .findByInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StudentResponse getStudentById(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Student student =
                studentRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found or does not belong to your institution"
                                ));

        return toResponse(student);
    }

    public List<StudentResponse> getStudentsByProgram(
            Long programId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return studentRepository
                .findByInstitutionIdAndProgramId(
                        institutionId,
                        programId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StudentResponse> getStudentsByClassSection(
            Long classSectionId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return studentRepository
                .findByInstitutionIdAndClassSectionId(
                        institutionId,
                        classSectionId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StudentResponse> getStudentsByStatus(
            StudentStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return studentRepository
                .findByInstitutionIdAndStatus(
                        institutionId,
                        status
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StudentResponse createStudent(
            StudentRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Institution institution =
                institutionRepository
                        .findById(institutionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Institution not found"
                                ));

        Program program =
                programRepository
                        .findById(request.getProgramId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Program not found"
                                ));

        /*
         * Verify that the program belongs
         * to the current institution.
         */
        if (!program.getDepartment()
                .getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Program does not belong to your institution"
            );
        }

        AcademicYear academicYear =
                academicYearRepository
                        .findById(request.getAcademicYearId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Academic year not found"
                                ));

        if (!academicYear.getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Academic year does not belong to your institution"
            );
        }

        ClassSection classSection = null;

        if (request.getClassSectionId() != null) {

            classSection =
                    classSectionRepository
                            .findById(
                                    request.getClassSectionId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Class section not found"
                                    ));

            if (!classSection
                    .getInstitution()
                    .getId()
                    .equals(institutionId)) {

                throw new RuntimeException(
                        "Class section does not belong to your institution"
                );
            }
        }

        if (studentRepository
                .existsByInstitutionIdAndEnrollmentNumber(
                        institutionId,
                        request.getEnrollmentNumber()
                )) {

            throw new RuntimeException(
                    "Student with this enrollment number already exists"
            );
        }

        if (studentRepository
                .existsByInstitutionIdAndEmail(
                        institutionId,
                        request.getEmail()
                )) {

            throw new RuntimeException(
                    "Student with this email already exists"
            );
        }

        /*
         * Create Student User Account.
         */
        UserRequest userRequest =
                new UserRequest();

        userRequest.setEmail(
                request.getEmail()
        );

        userRequest.setPersonalEmail(
                request.getPersonalEmail()
        );

        userRequest.setFirstName(
                request.getFirstName()
        );

        userRequest.setLastName(
                request.getLastName()
        );

        userRequest.setPhone(
                request.getPhone()
        );

        userRequest.setRole(
                "STUDENT"
        );

        userService.createUser(
                userRequest
        );

        /*
         * Create Student.
         */
        Student student =
                Student.builder()
                        .institution(institution)
                        .program(program)
                        .academicYear(academicYear)
                        .classSection(classSection)
                        .enrollmentNumber(
                                request.getEnrollmentNumber()
                        )
                        .firstName(
                                request.getFirstName()
                        )
                        .lastName(
                                request.getLastName()
                        )
                        .email(
                                request.getEmail()
                        )
                        .personalEmail(
                                request.getPersonalEmail()
                        )
                        .phone(
                                request.getPhone()
                        )
                        .admissionDate(
                                request.getAdmissionDate()
                        )
                        .status(
                                StudentStatus.ACTIVE
                        )
                        .build();

        Student savedStudent =
                studentRepository.save(student);

        return toResponse(savedStudent);
    }

    @Transactional
    public StudentResponse updateStudent(
            Long id,
            StudentRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Student student =
                studentRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found or does not belong to your institution"
                                ));

        Program program =
                programRepository
                        .findById(request.getProgramId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Program not found"
                                ));

        if (!program.getDepartment()
                .getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Program does not belong to your institution"
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

        if (!academicYear.getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Academic year does not belong to your institution"
            );
        }

        ClassSection classSection = null;

        if (request.getClassSectionId() != null) {

            classSection =
                    classSectionRepository
                            .findById(
                                    request.getClassSectionId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Class section not found"
                                    ));

            if (!classSection
                    .getInstitution()
                    .getId()
                    .equals(institutionId)) {

                throw new RuntimeException(
                        "Class section does not belong to your institution"
                );
            }
        }

        student.setProgram(program);
        student.setAcademicYear(academicYear);
        student.setClassSection(classSection);

        student.setEnrollmentNumber(
                request.getEnrollmentNumber()
        );

        student.setFirstName(
                request.getFirstName()
        );

        student.setLastName(
                request.getLastName()
        );

        student.setPersonalEmail(
                request.getPersonalEmail()
        );

        student.setPhone(
                request.getPhone()
        );

        student.setAdmissionDate(
                request.getAdmissionDate()
        );

        Student updatedStudent =
                studentRepository.save(student);

        return toResponse(updatedStudent);
    }

    public StudentResponse updateStudentStatus(
            Long id,
            StudentStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Student student =
                studentRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found or does not belong to your institution"
                                ));

        student.setStatus(status);

        return toResponse(
                studentRepository.save(student)
        );
    }

    public void deleteStudent(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Student student =
                studentRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found or does not belong to your institution"
                                ));

        if (student.getStatus() == StudentStatus.ACTIVE) {

            throw new RuntimeException(
                    "Active student cannot be deleted"
            );
        }

        studentRepository.delete(student);
    }

    private StudentResponse toResponse(
            Student student) {

        StudentResponse response =
                new StudentResponse();

        response.setId(
                student.getId()
        );

        response.setInstitutionId(
                student.getInstitution().getId()
        );

        response.setProgramId(
                student.getProgram().getId()
        );

        response.setProgramName(
                student.getProgram().getName()
        );

        if (student.getClassSection() != null) {

            response.setClassSectionId(
                    student.getClassSection().getId()
            );

            response.setClassSectionName(
                    student.getClassSection().getName()
            );
        }

        response.setAcademicYearId(
                student.getAcademicYear().getId()
        );

        response.setAcademicYearName(
                student.getAcademicYear().getName()
        );

        response.setEnrollmentNumber(
                student.getEnrollmentNumber()
        );

        response.setFirstName(
                student.getFirstName()
        );

        response.setLastName(
                student.getLastName()
        );

        response.setEmail(
                student.getEmail()
        );

        response.setPersonalEmail(
                student.getPersonalEmail()
        );

        response.setPhone(
                student.getPhone()
        );

        response.setAdmissionDate(
                student.getAdmissionDate()
        );

        response.setStatus(
                student.getStatus()
        );

        response.setCreatedAt(
                student.getCreatedAt()
        );

        response.setUpdatedAt(
                student.getUpdatedAt()
        );

        return response;
    }
}