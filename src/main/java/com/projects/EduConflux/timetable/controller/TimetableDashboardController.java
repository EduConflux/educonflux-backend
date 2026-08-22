package com.projects.EduConflux.timetable.controller;

import com.projects.EduConflux.timetable.dto.TimetableEntryResponse;
import com.projects.EduConflux.timetable.service.TimetableDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TimetableDashboardController {

    private final TimetableDashboardService timetableDashboardService;


    /*
     * STUDENT
     */

    @GetMapping("/api/student/timetable/today")
    public ResponseEntity<List<TimetableEntryResponse>>
    getStudentToday() {

        return ResponseEntity.ok(
                timetableDashboardService.getStudentToday()
        );
    }


    @GetMapping("/api/student/timetable")
    public ResponseEntity<List<TimetableEntryResponse>>
    getStudentWeekly() {

        return ResponseEntity.ok(
                timetableDashboardService.getStudentWeekly()
        );
    }


    /*
     * FACULTY
     */

    @GetMapping("/api/faculty/timetable/today")
    public ResponseEntity<List<TimetableEntryResponse>>
    getFacultyToday() {

        return ResponseEntity.ok(
                timetableDashboardService.getFacultyToday()
        );
    }


    @GetMapping("/api/faculty/timetable")
    public ResponseEntity<List<TimetableEntryResponse>>
    getFacultyWeekly() {

        return ResponseEntity.ok(
                timetableDashboardService.getFacultyWeekly()
        );
    }
}