package com.projects.EduConflux.timetable.controller;

import com.projects.EduConflux.timetable.dto.TimetableEntryRequest;
import com.projects.EduConflux.timetable.dto.TimetableEntryResponse;
import com.projects.EduConflux.timetable.entity.TimetableStatus;
import com.projects.EduConflux.timetable.service.TimetableEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("/api/admin/timetable")
@RequiredArgsConstructor
public class TimetableEntryController {

    private final TimetableEntryService timetableService;


    @GetMapping
    public ResponseEntity<List<TimetableEntryResponse>>
    getAllEntries() {

        return ResponseEntity.ok(
                timetableService.getAllEntries()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<TimetableEntryResponse>
    getEntryById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                timetableService.getEntryById(id)
        );
    }


    @GetMapping("/class-section/{classSectionId}")
    public ResponseEntity<List<TimetableEntryResponse>>
    getByClassSection(
            @PathVariable Long classSectionId) {

        return ResponseEntity.ok(
                timetableService
                        .getByClassSection(classSectionId)
        );
    }


    @PostMapping
    public ResponseEntity<TimetableEntryResponse>
    createEntry(
            @Valid
            @RequestBody
            TimetableEntryRequest request) {

        return ResponseEntity.ok(
                timetableService.createEntry(request)
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<TimetableEntryResponse>
    updateEntry(
            @PathVariable Long id,
            @Valid
            @RequestBody
            TimetableEntryRequest request) {

        return ResponseEntity.ok(
                timetableService.updateEntry(
                        id,
                        request
                )
        );
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<TimetableEntryResponse>
    updateStatus(
            @PathVariable Long id,
            @RequestParam TimetableStatus status) {

        return ResponseEntity.ok(
                timetableService.updateStatus(
                        id,
                        status
                )
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable Long id) {

        timetableService.deleteEntry(id);

        return ResponseEntity.noContent().build();
    }
}