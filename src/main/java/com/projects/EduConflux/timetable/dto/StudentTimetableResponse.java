package com.projects.EduConflux.timetable.dto;

import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
public class StudentTimetableResponse {

    private Long classSectionId;
    private String classSectionName;

    private DayOfWeek dayOfWeek;

    private List<TimetableEntryResponse> entries;
}