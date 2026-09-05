package com.projects.EduConflux.dashboard.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class StudentDashboardResponse {

    private Long studentId;
    private String studentName;
    private String email;
    private String enrollmentNumber;

    private int classroomCount;
    private int assignmentCount;
    private int learningMaterialCount;

    private List<UpcomingAssignment> upcomingAssignments;

    public StudentDashboardResponse() {
    }

    public StudentDashboardResponse(
            Long studentId,
            String studentName,
            String email,
            String enrollmentNumber,
            int classroomCount,
            int assignmentCount,
            int learningMaterialCount,
            List<UpcomingAssignment> upcomingAssignments
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.enrollmentNumber = enrollmentNumber;
        this.classroomCount = classroomCount;
        this.assignmentCount = assignmentCount;
        this.learningMaterialCount = learningMaterialCount;
        this.upcomingAssignments = upcomingAssignments;
    }

    @Getter
    public static class UpcomingAssignment {

        private Long assignmentId;
        private Long classroomId;
        private String classroomName;
        private String title;
        private LocalDateTime dueDate;
        private Integer maxMarks;

        public UpcomingAssignment() {
        }

        public UpcomingAssignment(
                Long assignmentId,
                Long classroomId,
                String classroomName,
                String title,
                LocalDateTime dueDate,
                Integer maxMarks
        ) {
            this.assignmentId = assignmentId;
            this.classroomId = classroomId;
            this.classroomName = classroomName;
            this.title = title;
            this.dueDate = dueDate;
            this.maxMarks = maxMarks;
        }

    }
}