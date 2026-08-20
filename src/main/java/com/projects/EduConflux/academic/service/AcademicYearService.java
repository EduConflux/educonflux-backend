package com.projects.EduConflux.academic.service;

import com.projects.EduConflux.academic.dto.AcademicYearRequest;
import com.projects.EduConflux.academic.dto.AcademicYearResponse;
import com.projects.EduConflux.academic.entity.AcademicYear;
import com.projects.EduConflux.academic.entity.AcademicYearStatus;
import com.projects.EduConflux.academic.repository.AcademicYearRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.institution.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final InstitutionRepository institutionRepository;

    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getAllAcademicYears() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        return academicYearRepository
                .findByInstitutionId(institutionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademicYearResponse getAcademicYearById(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        AcademicYear academicYear =
                academicYearRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Academic year not found"
                                ));

        return toResponse(academicYear);
    }

    @Transactional
    public AcademicYearResponse createAcademicYear(
            AcademicYearRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {

            throw new RuntimeException(
                    "End date cannot be before start date"
            );
        }

        if (academicYearRepository
                .existsByInstitutionIdAndName(
                        institutionId,
                        request.getName()
                )) {

            throw new RuntimeException(
                    "Academic year already exists"
            );
        }

        Institution institution =
                institutionRepository
                        .findById(institutionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Institution not found"
                                ));

        AcademicYear academicYear =
                AcademicYear.builder()
                        .institution(institution)
                        .name(request.getName())
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .status(AcademicYearStatus.INACTIVE)
                        .build();

        AcademicYear savedAcademicYear =
                academicYearRepository.save(
                        academicYear
                );

        return toResponse(savedAcademicYear);
    }

    @Transactional
    public AcademicYearResponse updateAcademicYear(
            Long id,
            AcademicYearRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {

            throw new RuntimeException(
                    "End date cannot be before start date"
            );
        }

        AcademicYear academicYear =
                academicYearRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Academic year not found"
                                ));

        boolean nameChanged =
                !academicYear.getName()
                        .equals(request.getName());

        if (nameChanged &&
                academicYearRepository
                        .existsByInstitutionIdAndName(
                                institutionId,
                                request.getName()
                        )) {

            throw new RuntimeException(
                    "Academic year already exists"
            );
        }

        academicYear.setName(
                request.getName()
        );

        academicYear.setStartDate(
                request.getStartDate()
        );

        academicYear.setEndDate(
                request.getEndDate()
        );

        AcademicYear updatedAcademicYear =
                academicYearRepository.save(
                        academicYear
                );

        return toResponse(updatedAcademicYear);
    }

    @Transactional
    public AcademicYearResponse updateAcademicYearStatus(
            Long id,
            AcademicYearStatus status) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        AcademicYear academicYear =
                academicYearRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Academic year not found"
                                ));

        academicYear.setStatus(status);

        AcademicYear updatedAcademicYear =
                academicYearRepository.save(
                        academicYear
                );

        return toResponse(updatedAcademicYear);
    }

    @Transactional
    public void deleteAcademicYear(Long id) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        AcademicYear academicYear =
                academicYearRepository
                        .findByIdAndInstitutionId(
                                id,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Academic year not found"
                                ));

        if (academicYear.getStatus()
                == AcademicYearStatus.ACTIVE) {

            throw new RuntimeException(
                    "Active academic year cannot be deleted"
            );
        }

        academicYearRepository.delete(
                academicYear
        );
    }

    private AcademicYearResponse toResponse(
            AcademicYear academicYear) {

        AcademicYearResponse response =
                new AcademicYearResponse();

        response.setId(
                academicYear.getId()
        );

        response.setInstitutionId(
                academicYear
                        .getInstitution()
                        .getId()
        );

        response.setName(
                academicYear.getName()
        );

        response.setStartDate(
                academicYear.getStartDate()
        );

        response.setEndDate(
                academicYear.getEndDate()
        );

        response.setStatus(
                academicYear.getStatus()
        );

        response.setCreatedAt(
                academicYear.getCreatedAt()
        );

        response.setUpdatedAt(
                academicYear.getUpdatedAt()
        );

        return response;
    }
}