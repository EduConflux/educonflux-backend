package com.projects.EduConflux.academic.service;

import com.projects.EduConflux.academic.dto.ProgramRequest;
import com.projects.EduConflux.academic.dto.ProgramResponse;
import com.projects.EduConflux.academic.entity.Department;
import com.projects.EduConflux.academic.entity.Program;
import com.projects.EduConflux.academic.entity.ProgramStatus;
import com.projects.EduConflux.academic.repository.ProgramRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.institution.repository.InstitutionRepository;
import com.projects.EduConflux.academic.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;
    private final InstitutionRepository institutionRepository;

    @Transactional(readOnly = true)
    public List<ProgramResponse> getAllPrograms() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return programRepository
                .findByInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgramResponse getProgramById(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Program program =
                programRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Program not found"
                                ));

        return toResponse(program);
    }

    @Transactional
    public ProgramResponse createProgram(
            ProgramRequest request) {

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

        if (programRepository
                .existsByDepartmentIdAndName(
                        department.getId(),
                        request.getName()
                )) {

            throw new RuntimeException(
                    "Program with this name already exists in the department"
            );
        }

        if (programRepository
                .existsByDepartmentIdAndCode(
                        department.getId(),
                        request.getCode()
                )) {

            throw new RuntimeException(
                    "Program with this code already exists in the department"
            );
        }

        Program program =
                Program.builder()
                        .institution(institution)
                        .department(department)
                        .name(request.getName())
                        .code(request.getCode())
                        .description(request.getDescription())
                        .durationYears(request.getDurationYears())
                        .status(ProgramStatus.ACTIVE)
                        .build();

        Program savedProgram =
                programRepository.save(program);

        return toResponse(savedProgram);
    }

    @Transactional
    public ProgramResponse updateProgram(
            Long id,
            ProgramRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Program program =
                programRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Program not found"
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

        boolean departmentChanged =
                !program.getDepartment()
                        .getId()
                        .equals(department.getId());

        boolean nameChanged =
                !program.getName()
                        .equals(request.getName());

        boolean codeChanged =
                !program.getCode()
                        .equals(request.getCode());

        if ((departmentChanged || nameChanged) &&
                programRepository
                        .existsByDepartmentIdAndName(
                                department.getId(),
                                request.getName()
                        )) {

            throw new RuntimeException(
                    "Program with this name already exists in the department"
            );
        }

        if ((departmentChanged || codeChanged) &&
                programRepository
                        .existsByDepartmentIdAndCode(
                                department.getId(),
                                request.getCode()
                        )) {

            throw new RuntimeException(
                    "Program with this code already exists in the department"
            );
        }

        program.setDepartment(department);
        program.setName(request.getName());
        program.setCode(request.getCode());
        program.setDescription(request.getDescription());
        program.setDurationYears(request.getDurationYears());

        Program updatedProgram =
                programRepository.save(program);

        return toResponse(updatedProgram);
    }

    @Transactional
    public ProgramResponse updateProgramStatus(
            Long id,
            ProgramStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Program program =
                programRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Program not found"
                                ));

        program.setStatus(status);

        Program updatedProgram =
                programRepository.save(program);

        return toResponse(updatedProgram);
    }

    @Transactional
    public void deleteProgram(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Program program =
                programRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Program not found"
                                ));

        if (program.getStatus()
                == ProgramStatus.ACTIVE) {

            throw new RuntimeException(
                    "Active program cannot be deleted"
            );
        }

        programRepository.delete(program);
    }

    private ProgramResponse toResponse(
            Program program) {

        ProgramResponse response =
                new ProgramResponse();

        response.setId(
                program.getId()
        );

        response.setInstitutionId(
                program.getInstitution()
                        .getId()
        );

        response.setDepartmentId(
                program.getDepartment()
                        .getId()
        );

        response.setDepartmentName(
                program.getDepartment()
                        .getName()
        );

        response.setName(
                program.getName()
        );

        response.setCode(
                program.getCode()
        );

        response.setDescription(
                program.getDescription()
        );

        response.setDurationYears(
                program.getDurationYears()
        );

        response.setStatus(
                program.getStatus()
        );

        response.setCreatedAt(
                program.getCreatedAt()
        );

        response.setUpdatedAt(
                program.getUpdatedAt()
        );

        return response;
    }
}