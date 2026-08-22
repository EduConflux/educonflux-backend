package com.projects.EduConflux.timetable.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class TimetableEntryRequest {

    @NotNull(message = "Class section ID is required")
    private Long classSectionId;

    @NotNull(message = "Course offering ID is required")
    private Long courseOfferingId;

    @NotNull(message = "Faculty assignment ID is required")
    private Long facultyAssignmentId;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private String room;
}