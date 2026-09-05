package com.projects.EduConflux.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

public class FacultyDashboardResponse {

    private Long facultyId;
    private String facultyName;
    private String email;

    private int classroomCount;
    private int studentCount;
    private int assignmentCount;
    private int learningMaterialCount;

    private List<UpcomingAssignment> upcomingAssignments;

    public FacultyDashboardResponse() {
    }

    public FacultyDashboardResponse(
            Long facultyId,
            String facultyName,
            String email,
            int classroomCount,
            int studentCount,
            int assignmentCount,
            int learningMaterialCount,
            List<UpcomingAssignment> upcomingAssignments
    ) {
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.email = email;
        this.classroomCount = classroomCount;
        this.studentCount = studentCount;
        this.assignmentCount = assignmentCount;
        this.learningMaterialCount = learningMaterialCount;
        this.upcomingAssignments = upcomingAssignments;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public String getEmail() {
        return email;
    }

    public int getClassroomCount() {
        return classroomCount;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public int getAssignmentCount() {
        return assignmentCount;
    }

    public int getLearningMaterialCount() {
        return learningMaterialCount;
    }

    public List<UpcomingAssignment> getUpcomingAssignments() {
        return upcomingAssignments;
    }

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

        public Long getAssignmentId() {
            return assignmentId;
        }

        public Long getClassroomId() {
            return classroomId;
        }

        public String getClassroomName() {
            return classroomName;
        }

        public String getTitle() {
            return title;
        }

        public LocalDateTime getDueDate() {
            return dueDate;
        }

        public Integer getMaxMarks() {
            return maxMarks;
        }
    }
}