package com.projects.EduConflux.timetable.dto;

import com.projects.EduConflux.timetable.entity.TimetableStatus;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class TimetableEntryResponse {

    private Long id;

    private Long institutionId;

    private Long classSectionId;
    private String classSectionName;

    private Long courseOfferingId;

    private Long courseId;
    private String courseCode;
    private String courseName;

    private Long facultyAssignmentId;

    private Long facultyId;
    private String facultyName;
    private String facultyEmployeeId;

    private Long programId;
    private String programName;

    private Long semesterId;
    private Integer semesterNumber;
    private String semesterName;

    private Long academicYearId;
    private String academicYearName;

    private DayOfWeek dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;

    private String room;

    private TimetableStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}