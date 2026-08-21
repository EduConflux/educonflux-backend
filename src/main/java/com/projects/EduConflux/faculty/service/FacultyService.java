package com.projects.EduConflux.faculty.service;

import com.projects.EduConflux.academic.entity.Department;
import com.projects.EduConflux.academic.repository.DepartmentRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.faculty.dto.FacultyRequest;
import com.projects.EduConflux.faculty.dto.FacultyResponse;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.entity.FacultyStatus;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.identity.dto.UserRequest;
import com.projects.EduConflux.identity.service.UserService;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.institution.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final InstitutionRepository institutionRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<FacultyResponse> getAllFaculty() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return facultyRepository
                .findByInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FacultyResponse> getFacultyByDepartment(
            Long departmentId) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        departmentRepository
                .findByIdAndInstitutionId(
                        departmentId,
                        institutionId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Department not found or does not belong to your institution"
                        ));

        return facultyRepository
                .findByDepartmentIdAndInstitutionId(
                        departmentId,
                        institutionId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FacultyResponse getFacultyById(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Faculty faculty =
                facultyRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty not found"
                                ));

        return toResponse(faculty);
    }

    @Transactional
    public FacultyResponse createFaculty(
            FacultyRequest request) {

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

        if (facultyRepository
                .existsByInstitutionIdAndEmployeeId(
                        institutionId,
                        request.getEmployeeId()
                )) {

            throw new RuntimeException(
                    "Faculty with this employee ID already exists"
            );
        }

        /*
         * Create the EduConflux user account.
         */
        UserRequest userRequest = new UserRequest();

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
                "FACULTY"
        );

        userService.createUser(userRequest);

        /*
         * Create the Faculty record.
         */
        Faculty faculty =
                Faculty.builder()
                        .institution(institution)
                        .department(department)
                        .employeeId(request.getEmployeeId())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .personalEmail(request.getPersonalEmail())
                        .phone(request.getPhone())
                        .designation(request.getDesignation())
                        .qualification(request.getQualification())
                        .joiningDate(request.getJoiningDate())
                        .status(FacultyStatus.ACTIVE)
                        .build();

        Faculty savedFaculty =
                facultyRepository.save(faculty);

        return toResponse(savedFaculty);
    }

    @Transactional
    public FacultyResponse updateFaculty(
            Long id,
            FacultyRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Faculty faculty =
                facultyRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty not found"
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

        boolean employeeIdChanged =
                !faculty.getEmployeeId()
                        .equals(request.getEmployeeId());

        if (employeeIdChanged &&
                facultyRepository
                        .existsByInstitutionIdAndEmployeeId(
                                institutionId,
                                request.getEmployeeId()
                        )) {

            throw new RuntimeException(
                    "Faculty with this employee ID already exists"
            );
        }

        faculty.setDepartment(department);
        faculty.setEmployeeId(request.getEmployeeId());
        faculty.setFirstName(request.getFirstName());
        faculty.setLastName(request.getLastName());
        faculty.setEmail(request.getEmail());
        faculty.setPersonalEmail(
                request.getPersonalEmail()
        );
        faculty.setPhone(request.getPhone());
        faculty.setDesignation(request.getDesignation());
        faculty.setQualification(request.getQualification());
        faculty.setJoiningDate(request.getJoiningDate());

        Faculty updatedFaculty =
                facultyRepository.save(faculty);

        return toResponse(updatedFaculty);
    }

    @Transactional
    public FacultyResponse updateFacultyStatus(
            Long id,
            FacultyStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Faculty faculty =
                facultyRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty not found"
                                ));

        faculty.setStatus(status);

        Faculty updatedFaculty =
                facultyRepository.save(faculty);

        return toResponse(updatedFaculty);
    }

    @Transactional
    public void deleteFaculty(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Faculty faculty =
                facultyRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty not found"
                                ));

        if (faculty.getStatus()
                == FacultyStatus.ACTIVE) {

            throw new RuntimeException(
                    "Active faculty cannot be deleted"
            );
        }

        facultyRepository.delete(faculty);
    }

    private FacultyResponse toResponse(
            Faculty faculty) {

        FacultyResponse response =
                new FacultyResponse();

        response.setId(
                faculty.getId()
        );

        response.setInstitutionId(
                faculty.getInstitution()
                        .getId()
        );

        response.setDepartmentId(
                faculty.getDepartment()
                        .getId()
        );

        response.setDepartmentName(
                faculty.getDepartment()
                        .getName()
        );

        response.setEmployeeId(
                faculty.getEmployeeId()
        );

        response.setFirstName(
                faculty.getFirstName()
        );

        response.setLastName(
                faculty.getLastName()
        );

        response.setEmail(
                faculty.getEmail()
        );

        response.setPhone(
                faculty.getPhone()
        );

        response.setDesignation(
                faculty.getDesignation()
        );

        response.setQualification(
                faculty.getQualification()
        );

        response.setJoiningDate(
                faculty.getJoiningDate()
        );

        response.setStatus(
                faculty.getStatus()
        );

        response.setCreatedAt(
                faculty.getCreatedAt()
        );

        response.setUpdatedAt(
                faculty.getUpdatedAt()
        );

        response.setPersonalEmail(
                faculty.getPersonalEmail()
        );

        return response;
    }
}