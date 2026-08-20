package com.projects.EduConflux.academic.service;

import com.projects.EduConflux.academic.dto.DepartmentRequest;
import com.projects.EduConflux.academic.dto.DepartmentResponse;
import com.projects.EduConflux.academic.entity.Department;
import com.projects.EduConflux.academic.entity.DepartmentStatus;
import com.projects.EduConflux.academic.repository.DepartmentRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.institution.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final InstitutionRepository institutionRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return departmentRepository
                .findByInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Department department =
                departmentRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found"
                                ));

        return toResponse(department);
    }

    @Transactional
    public DepartmentResponse createDepartment(
            DepartmentRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        if (departmentRepository
                .existsByInstitutionIdAndName(
                        institutionId,
                        request.getName()
                )) {

            throw new RuntimeException(
                    "Department with this name already exists"
            );
        }

        if (departmentRepository
                .existsByInstitutionIdAndCode(
                        institutionId,
                        request.getCode()
                )) {

            throw new RuntimeException(
                    "Department with this code already exists"
            );
        }

        Institution institution =
                institutionRepository
                        .findById(institutionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Institution not found"
                                ));

        Department department =
                Department.builder()
                        .institution(institution)
                        .name(request.getName())
                        .code(request.getCode())
                        .description(request.getDescription())
                        .status(DepartmentStatus.ACTIVE)
                        .build();

        Department savedDepartment =
                departmentRepository.save(department);

        return toResponse(savedDepartment);
    }

    @Transactional
    public DepartmentResponse updateDepartment(
            Long id,
            DepartmentRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Department department =
                departmentRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found"
                                ));

        boolean nameChanged =
                !department.getName()
                        .equals(request.getName());

        if (nameChanged &&
                departmentRepository
                        .existsByInstitutionIdAndName(
                                institutionId,
                                request.getName()
                        )) {

            throw new RuntimeException(
                    "Department with this name already exists"
            );
        }

        boolean codeChanged =
                !department.getCode()
                        .equals(request.getCode());

        if (codeChanged &&
                departmentRepository
                        .existsByInstitutionIdAndCode(
                                institutionId,
                                request.getCode()
                        )) {

            throw new RuntimeException(
                    "Department with this code already exists"
            );
        }

        department.setName(
                request.getName()
        );

        department.setCode(
                request.getCode()
        );

        department.setDescription(
                request.getDescription()
        );

        Department updatedDepartment =
                departmentRepository.save(
                        department
                );

        return toResponse(updatedDepartment);
    }

    @Transactional
    public DepartmentResponse updateDepartmentStatus(
            Long id,
            DepartmentStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Department department =
                departmentRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found"
                                ));

        department.setStatus(status);

        Department updatedDepartment =
                departmentRepository.save(
                        department
                );

        return toResponse(updatedDepartment);
    }

    @Transactional
    public void deleteDepartment(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Department department =
                departmentRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found"
                                ));

        if (department.getStatus()
                == DepartmentStatus.ACTIVE) {

            throw new RuntimeException(
                    "Active department cannot be deleted"
            );
        }

        departmentRepository.delete(department);
    }

    private DepartmentResponse toResponse(
            Department department) {

        DepartmentResponse response =
                new DepartmentResponse();

        response.setId(
                department.getId()
        );

        response.setInstitutionId(
                department
                        .getInstitution()
                        .getId()
        );

        response.setName(
                department.getName()
        );

        response.setCode(
                department.getCode()
        );

        response.setDescription(
                department.getDescription()
        );

        response.setStatus(
                department.getStatus()
        );

        response.setCreatedAt(
                department.getCreatedAt()
        );

        response.setUpdatedAt(
                department.getUpdatedAt()
        );

        return response;
    }
}