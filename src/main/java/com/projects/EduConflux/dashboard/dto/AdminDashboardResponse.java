package com.projects.EduConflux.dashboard.dto;

import lombok.Getter;

@Getter
public class AdminDashboardResponse {

    private Long institutionId;
    private String institutionName;

    private long totalUsers;
    private long totalStudents;
    private long totalFaculty;
    private long totalClassrooms;
    private long totalAssignments;
    private long totalLearningMaterials;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(
            Long institutionId,
            String institutionName,
            long totalUsers,
            long totalStudents,
            long totalFaculty,
            long totalClassrooms,
            long totalAssignments,
            long totalLearningMaterials
    ) {
        this.institutionId = institutionId;
        this.institutionName = institutionName;
        this.totalUsers = totalUsers;
        this.totalStudents = totalStudents;
        this.totalFaculty = totalFaculty;
        this.totalClassrooms = totalClassrooms;
        this.totalAssignments = totalAssignments;
        this.totalLearningMaterials = totalLearningMaterials;
    }

}