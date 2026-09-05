package com.projects.EduConflux.dashboard.controller;

import com.projects.EduConflux.dashboard.dto.FacultyDashboardResponse;
import com.projects.EduConflux.dashboard.dto.StudentDashboardResponse;
import com.projects.EduConflux.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.projects.EduConflux.dashboard.dto.AdminDashboardResponse;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/student/dashboard")
    public ResponseEntity<StudentDashboardResponse>
    getStudentDashboard() {

        return ResponseEntity.ok(
                dashboardService.getStudentDashboard()
        );
    }

    @GetMapping("/faculty/dashboard")
    public ResponseEntity<FacultyDashboardResponse>
    getFacultyDashboard() {

        return ResponseEntity.ok(
                dashboardService.getFacultyDashboard()
        );
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<AdminDashboardResponse>
    getAdminDashboard() {

        return ResponseEntity.ok(
                dashboardService.getAdminDashboard()
        );
    }
}