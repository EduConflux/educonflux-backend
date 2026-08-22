package com.projects.EduConflux.attendance.repository;

import com.projects.EduConflux.attendance.entity.Attendance;
import com.projects.EduConflux.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByIdAndStudentInstitutionId(
            Long id,
            Long institutionId
    );

    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(
            Long studentId
    );

    List<Attendance> findByStudentIdAndAttendanceDate(
            Long studentId,
            LocalDate attendanceDate
    );

    List<Attendance> findByTimetableEntryIdAndAttendanceDate(
            Long timetableEntryId,
            LocalDate attendanceDate
    );

    List<Attendance> findByStudentIdAndStatus(
            Long studentId,
            AttendanceStatus status
    );

    boolean existsByStudentIdAndTimetableEntryIdAndAttendanceDate(
            Long studentId,
            Long timetableEntryId,
            LocalDate attendanceDate
    );

    Optional<Attendance> findByStudentIdAndTimetableEntryIdAndAttendanceDate(
            Long studentId,
            Long timetableEntryId,
            LocalDate attendanceDate
    );
}