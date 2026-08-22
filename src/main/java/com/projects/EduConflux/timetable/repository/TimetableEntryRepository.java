package com.projects.EduConflux.timetable.repository;

import com.projects.EduConflux.timetable.entity.TimetableEntry;
import com.projects.EduConflux.timetable.entity.TimetableStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimetableEntryRepository
        extends JpaRepository<TimetableEntry, Long> {

    List<TimetableEntry> findByInstitutionId(
            Long institutionId
    );

    Optional<TimetableEntry> findByIdAndInstitutionId(
            Long id,
            Long institutionId
    );

    List<TimetableEntry> findByInstitutionIdAndClassSectionId(
            Long institutionId,
            Long classSectionId
    );

    List<TimetableEntry> findByInstitutionIdAndDayOfWeek(
            Long institutionId,
            DayOfWeek dayOfWeek
    );

    List<TimetableEntry> findByInstitutionIdAndStatus(
            Long institutionId,
            TimetableStatus status
    );

    boolean existsByInstitutionIdAndClassSectionIdAndDayOfWeekAndStartTime(
            Long institutionId,
            Long classSectionId,
            DayOfWeek dayOfWeek,
            LocalTime startTime
    );

    List<TimetableEntry> findByInstitutionIdAndFacultyAssignmentFacultyId(
            Long institutionId,
            Long facultyId
    );

    List<TimetableEntry> findByInstitutionIdAndClassSectionIdAndDayOfWeek(
            Long institutionId,
            Long classSectionId,
            java.time.DayOfWeek dayOfWeek
    );

    List<TimetableEntry> findByInstitutionIdAndFacultyAssignmentFacultyIdAndDayOfWeek(
            Long institutionId,
            Long facultyId,
            java.time.DayOfWeek dayOfWeek
    );
}