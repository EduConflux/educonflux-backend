
package com.projects.EduConflux.parent.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParentAttendanceSummaryResponse {

    private Long studentId;
    private String studentName;

    private long totalClasses;
    private long present;
    private long absent;
    private long late;

    private double attendancePercentage;
}
