package com.projects.EduConflux.academic.service;

import com.projects.EduConflux.academic.dto.ClassSectionRequest;
import com.projects.EduConflux.academic.dto.ClassSectionResponse;
import com.projects.EduConflux.academic.entity.ClassSection;
import com.projects.EduConflux.academic.entity.ClassSectionStatus;
import com.projects.EduConflux.academic.entity.ProgramStatus;
import com.projects.EduConflux.academic.entity.Semester;
import com.projects.EduConflux.academic.entity.SemesterStatus;
import com.projects.EduConflux.academic.repository.ClassSectionRepository;
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
public class ClassSectionService {

    private final ClassSectionRepository classSectionRepository;
    private final SemesterRepository semesterRepository;
    private final InstitutionRepository institutionRepository;

    @Transactional(readOnly = true)
    public List<ClassSectionResponse> getAllClassSections() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return classSectionRepository
                .findByInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClassSectionResponse> getClassSectionsBySemester(
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

        return classSectionRepository
                .findBySemesterIdAndInstitutionId(
                        semesterId,
                        institutionId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClassSectionResponse getClassSectionById(
            Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        ClassSection classSection =
                classSectionRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Class section not found"
                                ));

        return toResponse(classSection);
    }

    @Transactional
    public ClassSectionResponse createClassSection(
            ClassSectionRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        Institution institution =
                institutionRepository
                        .findById(institutionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Institution not found"
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

        if (semester.getStatus() != SemesterStatus.ACTIVE) {

            throw new RuntimeException(
                    "Cannot create class section for an inactive or completed semester"
            );
        }

        if (semester.getProgram().getStatus()
                != ProgramStatus.ACTIVE) {

            throw new RuntimeException(
                    "Cannot create class section for an inactive program"
            );
        }

        if (classSectionRepository
                .existsBySemesterIdAndName(
                        semester.getId(),
                        request.getName()
                )) {

            throw new RuntimeException(
                    "Class section with this name already exists for this semester"
            );
        }

        ClassSection classSection =
                ClassSection.builder()
                        .institution(institution)
                        .semester(semester)
                        .name(request.getName())
                        .description(request.getDescription())
                        .status(ClassSectionStatus.ACTIVE)
                        .build();

        ClassSection savedClassSection =
                classSectionRepository.save(classSection);

        return toResponse(savedClassSection);
    }

    @Transactional
    public ClassSectionResponse updateClassSection(
            Long id,
            ClassSectionRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        ClassSection classSection =
                classSectionRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Class section not found"
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

        boolean semesterChanged =
                !classSection.getSemester()
                        .getId()
                        .equals(semester.getId());

        boolean nameChanged =
                !classSection.getName()
                        .equals(request.getName());

        if ((semesterChanged || nameChanged) &&
                classSectionRepository
                        .existsBySemesterIdAndName(
                                semester.getId(),
                                request.getName()
                        )) {

            throw new RuntimeException(
                    "Class section with this name already exists for this semester"
            );
        }

        classSection.setSemester(semester);
        classSection.setName(
                request.getName()
        );
        classSection.setDescription(
                request.getDescription()
        );

        ClassSection updatedClassSection =
                classSectionRepository.save(
                        classSection
                );

        return toResponse(updatedClassSection);
    }

    @Transactional
    public ClassSectionResponse updateClassSectionStatus(
            Long id,
            ClassSectionStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        ClassSection classSection =
                classSectionRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Class section not found"
                                ));

        classSection.setStatus(status);

        ClassSection updatedClassSection =
                classSectionRepository.save(
                        classSection
                );

        return toResponse(updatedClassSection);
    }

    @Transactional
    public void deleteClassSection(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        ClassSection classSection =
                classSectionRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Class section not found"
                                ));

        if (classSection.getStatus()
                == ClassSectionStatus.ACTIVE) {

            throw new RuntimeException(
                    "Active class section cannot be deleted"
            );
        }

        classSectionRepository.delete(
                classSection
        );
    }

    private ClassSectionResponse toResponse(
            ClassSection classSection) {

        ClassSectionResponse response =
                new ClassSectionResponse();

        response.setId(
                classSection.getId()
        );

        response.setInstitutionId(
                classSection.getInstitution()
                        .getId()
        );

        response.setSemesterId(
                classSection.getSemester()
                        .getId()
        );

        response.setSemesterName(
                classSection.getSemester()
                        .getName()
        );

        response.setSemesterNumber(
                classSection.getSemester()
                        .getSemesterNumber()
        );

        response.setProgramId(
                classSection.getSemester()
                        .getProgram()
                        .getId()
        );

        response.setProgramName(
                classSection.getSemester()
                        .getProgram()
                        .getName()
        );

        response.setName(
                classSection.getName()
        );

        response.setDescription(
                classSection.getDescription()
        );

        response.setStatus(
                classSection.getStatus()
        );

        response.setCreatedAt(
                classSection.getCreatedAt()
        );

        response.setUpdatedAt(
                classSection.getUpdatedAt()
        );

        return response;
    }
}