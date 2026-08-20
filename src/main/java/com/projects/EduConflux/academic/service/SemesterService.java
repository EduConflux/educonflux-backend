package com.projects.EduConflux.academic.service;

import com.projects.EduConflux.academic.dto.SemesterRequest;
import com.projects.EduConflux.academic.dto.SemesterResponse;
import com.projects.EduConflux.academic.entity.Program;
import com.projects.EduConflux.academic.entity.Semester;
import com.projects.EduConflux.academic.entity.SemesterStatus;
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
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final ProgramRepository programRepository;
    private final InstitutionRepository institutionRepository;

    @Transactional(readOnly = true)
    public List<SemesterResponse> getAllSemesters() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return semesterRepository
                .findByInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> getSemestersByProgram(
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

        return semesterRepository
                .findByProgramIdAndInstitutionId(
                        programId,
                        institutionId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SemesterResponse getSemesterById(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Semester semester =
                semesterRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Semester not found"
                                ));

        return toResponse(semester);
    }

    @Transactional
    public SemesterResponse createSemester(
            SemesterRequest request) {

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
                        .findByIdAndInstitutionId(
                                request.getProgramId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Program not found or does not belong to your institution"
                                ));

        if (program.getStatus() !=
                com.projects.EduConflux.academic.entity.ProgramStatus.ACTIVE) {

            throw new RuntimeException(
                    "Cannot create semester for an inactive program"
            );
        }

        if (semesterRepository
                .existsByProgramIdAndSemesterNumber(
                        program.getId(),
                        request.getSemesterNumber()
                )) {

            throw new RuntimeException(
                    "Semester number already exists for this program"
            );
        }

        Semester semester =
                Semester.builder()
                        .institution(institution)
                        .program(program)
                        .semesterNumber(
                                request.getSemesterNumber()
                        )
                        .name(request.getName())
                        .status(SemesterStatus.ACTIVE)
                        .build();

        Semester savedSemester =
                semesterRepository.save(semester);

        return toResponse(savedSemester);
    }

    @Transactional
    public SemesterResponse updateSemester(
            Long id,
            SemesterRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Semester semester =
                semesterRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Semester not found"
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

        boolean programChanged =
                !semester.getProgram()
                        .getId()
                        .equals(program.getId());

        boolean numberChanged =
                !semester.getSemesterNumber()
                        .equals(request.getSemesterNumber());

        if ((programChanged || numberChanged) &&
                semesterRepository
                        .existsByProgramIdAndSemesterNumber(
                                program.getId(),
                                request.getSemesterNumber()
                        )) {

            throw new RuntimeException(
                    "Semester number already exists for this program"
            );
        }

        semester.setProgram(program);
        semester.setSemesterNumber(
                request.getSemesterNumber()
        );
        semester.setName(
                request.getName()
        );

        Semester updatedSemester =
                semesterRepository.save(semester);

        return toResponse(updatedSemester);
    }

    @Transactional
    public SemesterResponse updateSemesterStatus(
            Long id,
            SemesterStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Semester semester =
                semesterRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Semester not found"
                                ));

        semester.setStatus(status);

        Semester updatedSemester =
                semesterRepository.save(semester);

        return toResponse(updatedSemester);
    }

    @Transactional
    public void deleteSemester(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Semester semester =
                semesterRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Semester not found"
                                ));

        if (semester.getStatus()
                == SemesterStatus.ACTIVE) {

            throw new RuntimeException(
                    "Active semester cannot be deleted"
            );
        }

        semesterRepository.delete(semester);
    }

    private SemesterResponse toResponse(
            Semester semester) {

        SemesterResponse response =
                new SemesterResponse();

        response.setId(
                semester.getId()
        );

        response.setInstitutionId(
                semester.getInstitution()
                        .getId()
        );

        response.setProgramId(
                semester.getProgram()
                        .getId()
        );

        response.setProgramName(
                semester.getProgram()
                        .getName()
        );

        response.setSemesterNumber(
                semester.getSemesterNumber()
        );

        response.setName(
                semester.getName()
        );

        response.setStatus(
                semester.getStatus()
        );

        response.setCreatedAt(
                semester.getCreatedAt()
        );

        response.setUpdatedAt(
                semester.getUpdatedAt()
        );

        return response;
    }
}