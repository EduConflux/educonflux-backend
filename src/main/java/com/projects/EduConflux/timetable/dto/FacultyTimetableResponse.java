package com.projects.EduConflux.timetable.dto;

import lombok.Data;

import java.time.DayOfWeek;
import java.util.List;

@Data
public class FacultyTimetableResponse {

    private Long facultyId;
    private String facultyName;

    private DayOfWeek dayOfWeek;

    private List<TimetableEntryResponse> entries;
}