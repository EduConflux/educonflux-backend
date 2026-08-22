package com.projects.EduConflux.attendance.controller;

import com.projects.EduConflux.attendance.dto.AttendanceRequest;
import com.projects.EduConflux.attendance.dto.AttendanceResponse;
import com.projects.EduConflux.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;


    /*
     * FACULTY
     *
     * Mark attendance for a student.
     */
    @PostMapping("/api/faculty/attendance")
    public ResponseEntity<AttendanceResponse> markAttendance(
            @Valid @RequestBody AttendanceRequest request) {

        return ResponseEntity.ok(
                attendanceService.markAttendance(request)
        );
    }


    /*
     * FACULTY
     *
     * View attendance already marked
     * for one of their timetable classes.
     */
    @GetMapping("/api/faculty/attendance")
    public ResponseEntity<List<AttendanceResponse>>
    getFacultyAttendance(
            @RequestParam Long timetableEntryId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return ResponseEntity.ok(
                attendanceService.getFacultyAttendance(
                        timetableEntryId,
                        date
                )
        );
    }


    /*
     * STUDENT
     *
     * View complete personal attendance.
     */
    @GetMapping("/api/student/attendance")
    public ResponseEntity<List<AttendanceResponse>>
    getStudentAttendance() {

        return ResponseEntity.ok(
                attendanceService.getStudentAttendance()
        );
    }


    /*
     * STUDENT
     *
     * View personal attendance for a
     * particular date.
     */
    @GetMapping("/api/student/attendance/date")
    public ResponseEntity<List<AttendanceResponse>>
    getStudentAttendanceByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return ResponseEntity.ok(
                attendanceService.getStudentAttendanceByDate(
                        date
                )
        );
    }
}