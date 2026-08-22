package com.projects.EduConflux.attendance.dto;

import com.projects.EduConflux.attendance.entity.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AttendanceResponse {

    private Long id;

    private Long studentId;
    private String studentName;
    private String enrollmentNumber;

    private Long timetableEntryId;

    private Long classSectionId;
    private String classSectionName;

    private Long courseId;
    private String courseCode;
    private String courseName;

    private Long facultyId;
    private String facultyName;

    private LocalDate attendanceDate;

    private AttendanceStatus status;

    private String remarks;

    private LocalDateTime markedAt;
    private LocalDateTime updatedAt;
}