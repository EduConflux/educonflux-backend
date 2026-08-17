package com.projects.EduConflux.institution.service;

import com.projects.EduConflux.institution.dto.InstitutionRequest;
import com.projects.EduConflux.institution.dto.InstitutionResponse;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.institution.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;

    public List<InstitutionResponse> getAllInstitutions() {
        return institutionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public InstitutionResponse getInstitutionById(Long id) {

        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        return toResponse(institution);
    }

    public InstitutionResponse createInstitution(InstitutionRequest request) {

        Institution institution = new Institution();

        institution.setName(request.getName());
        institution.setCode(request.getCode());
        institution.setEmail(request.getEmail());
        institution.setPhone(request.getPhone());
        institution.setWebsite(request.getWebsite());
        institution.setLogoUrl(request.getLogoUrl());
        institution.setAddress(request.getAddress());
        institution.setCity(request.getCity());
        institution.setState(request.getState());
        institution.setCountry(request.getCountry());
        institution.setPostalCode(request.getPostalCode());

        Institution savedInstitution = institutionRepository.save(institution);

        return toResponse(savedInstitution);
    }

    private InstitutionResponse toResponse(Institution institution) {

        InstitutionResponse response = new InstitutionResponse();

        response.setId(institution.getId());
        response.setName(institution.getName());
        response.setCode(institution.getCode());
        response.setEmail(institution.getEmail());
        response.setPhone(institution.getPhone());
        response.setWebsite(institution.getWebsite());
        response.setLogoUrl(institution.getLogoUrl());
        response.setAddress(institution.getAddress());
        response.setCity(institution.getCity());
        response.setState(institution.getState());
        response.setCountry(institution.getCountry());
        response.setPostalCode(institution.getPostalCode());
        response.setStatus(institution.getStatus());
        response.setCreatedAt(institution.getCreatedAt());
        response.setUpdatedAt(institution.getUpdatedAt());

        return response;
    }

    public InstitutionResponse updateInstitution(
            Long id,
            InstitutionRequest request) {

        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        institution.setName(request.getName());
        institution.setCode(request.getCode());
        institution.setEmail(request.getEmail());
        institution.setPhone(request.getPhone());
        institution.setWebsite(request.getWebsite());
        institution.setLogoUrl(request.getLogoUrl());
        institution.setAddress(request.getAddress());
        institution.setCity(request.getCity());
        institution.setState(request.getState());
        institution.setCountry(request.getCountry());
        institution.setPostalCode(request.getPostalCode());

        Institution updatedInstitution =
                institutionRepository.save(institution);

        return toResponse(updatedInstitution);
    }

    public void deleteInstitution(Long id) {

        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        institutionRepository.delete(institution);
    }
}