package com.projects.EduConflux.institution.controller;

import com.projects.EduConflux.institution.dto.InstitutionRequest;
import com.projects.EduConflux.institution.dto.InstitutionResponse;
import com.projects.EduConflux.institution.entity.InstitutionStatus;
import com.projects.EduConflux.institution.service.InstitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    @GetMapping
    public ResponseEntity<List<InstitutionResponse>> getAllInstitutions() {
        return ResponseEntity.ok(
                institutionService.getAllInstitutions()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstitutionResponse> getInstitutionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                institutionService.getInstitutionById(id)
        );
    }

    @PostMapping
    public ResponseEntity<InstitutionResponse> createInstitution(
            @Valid @RequestBody InstitutionRequest request) {

        return ResponseEntity.ok(
                institutionService.createInstitution(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstitutionResponse> updateInstitution(
            @PathVariable Long id,
            @Valid @RequestBody InstitutionRequest request) {

        return ResponseEntity.ok(
                institutionService.updateInstitution(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstitution(
            @PathVariable Long id) {

        institutionService.deleteInstitution(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InstitutionResponse> updateInstitutionStatus(
            @PathVariable Long id,
            @RequestParam InstitutionStatus status) {

        return ResponseEntity.ok(
                institutionService.updateInstitutionStatus(id, status)
        );
    }
}