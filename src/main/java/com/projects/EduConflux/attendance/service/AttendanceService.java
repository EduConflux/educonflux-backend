package com.projects.EduConflux.attendance.service;

import com.projects.EduConflux.attendance.dto.AttendanceRequest;
import com.projects.EduConflux.attendance.dto.AttendanceResponse;
import com.projects.EduConflux.attendance.entity.Attendance;
import com.projects.EduConflux.attendance.repository.AttendanceRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import com.projects.EduConflux.timetable.entity.TimetableEntry;
import com.projects.EduConflux.timetable.repository.TimetableEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final TimetableEntryRepository timetableEntryRepository;


    @Transactional
    public AttendanceResponse markAttendance(
            AttendanceRequest request) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String facultyEmail =
                SecurityUtils.getCurrentUserEmail();


        /*
         * Find the faculty profile using
         * the email stored in the JWT.
         */
        Faculty faculty =
                facultyRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                facultyEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty profile not found"
                                ));


        /*
         * Find timetable entry.
         */
        TimetableEntry timetableEntry =
                timetableEntryRepository
                        .findById(
                                request.getTimetableEntryId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Timetable entry not found"
                                ));


        /*
         * Make sure timetable belongs
         * to the current institution.
         */
        if (!timetableEntry
                .getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Timetable entry does not belong to your institution"
            );
        }


        /*
         * Only the faculty assigned to this
         * timetable entry can mark attendance.
         */
        if (!timetableEntry
                .getFacultyAssignment()
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not assigned to this timetable entry"
            );
        }


        /*
         * Find student.
         */
        Student student =
                studentRepository
                        .findByIdAndInstitutionId(
                                request.getStudentId(),
                                institutionId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found"
                                ));


        /*
         * Student must belong to the same
         * class section as the timetable.
         */
        if (student.getClassSection() == null ||
                !student.getClassSection()
                        .getId()
                        .equals(
                                timetableEntry
                                        .getClassSection()
                                        .getId()
                        )) {

            throw new RuntimeException(
                    "Student does not belong to this class section"
            );
        }


        /*
         * Prevent duplicate attendance.
         */
        if (attendanceRepository
                .existsByStudentIdAndTimetableEntryIdAndAttendanceDate(
                        student.getId(),
                        timetableEntry.getId(),
                        request.getAttendanceDate()
                )) {

            throw new RuntimeException(
                    "Attendance already marked for this student"
            );
        }


        /*
         * Create attendance record.
         */
        Attendance attendance =
                Attendance.builder()
                        .student(student)
                        .timetableEntry(timetableEntry)
                        .attendanceDate(
                                request.getAttendanceDate()
                        )
                        .status(
                                request.getStatus()
                        )
                        .remarks(
                                request.getRemarks()
                        )
                        .build();


        attendance =
                attendanceRepository.save(
                        attendance
                );


        return mapToResponse(attendance);
    }


    @Transactional(readOnly = true)
    public List<AttendanceResponse> getStudentAttendance() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String studentEmail =
                SecurityUtils.getCurrentUserEmail();


        Student student =
                studentRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                studentEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student profile not found"
                                ));


        return attendanceRepository
                .findByStudentIdOrderByAttendanceDateDesc(
                        student.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<AttendanceResponse> getStudentAttendanceByDate(
            LocalDate date) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String studentEmail =
                SecurityUtils.getCurrentUserEmail();


        Student student =
                studentRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                studentEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student profile not found"
                                ));


        return attendanceRepository
                .findByStudentIdAndAttendanceDate(
                        student.getId(),
                        date
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<AttendanceResponse> getFacultyAttendance(
            Long timetableEntryId,
            LocalDate date) {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String facultyEmail =
                SecurityUtils.getCurrentUserEmail();


        Faculty faculty =
                facultyRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                facultyEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty profile not found"
                                ));


        TimetableEntry timetableEntry =
                timetableEntryRepository
                        .findById(
                                timetableEntryId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Timetable entry not found"
                                ));


        /*
         * Institution isolation.
         */
        if (!timetableEntry
                .getInstitution()
                .getId()
                .equals(institutionId)) {

            throw new RuntimeException(
                    "Timetable entry does not belong to your institution"
            );
        }


        /*
         * Faculty ownership check.
         */
        if (!timetableEntry
                .getFacultyAssignment()
                .getFaculty()
                .getId()
                .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not assigned to this timetable entry"
            );
        }


        return attendanceRepository
                .findByTimetableEntryIdAndAttendanceDate(
                        timetableEntryId,
                        date
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public AttendanceResponse mapToResponse(
            Attendance attendance) {

        Student student =
                attendance.getStudent();

        TimetableEntry timetable =
                attendance.getTimetableEntry();


        return AttendanceResponse.builder()

                .id(
                        attendance.getId()
                )

                .studentId(
                        student.getId()
                )

                .studentName(
                        student.getFirstName()
                                + " "
                                + student.getLastName()
                )

                .enrollmentNumber(
                        student.getEnrollmentNumber()
                )

                .timetableEntryId(
                        timetable.getId()
                )

                .classSectionId(
                        timetable
                                .getClassSection()
                                .getId()
                )

                .classSectionName(
                        timetable
                                .getClassSection()
                                .getName()
                )

                .courseId(
                        timetable
                                .getCourseOffering()
                                .getCourse()
                                .getId()
                )

                .courseCode(
                        timetable
                                .getCourseOffering()
                                .getCourse()
                                .getCode()
                )

                .courseName(
                        timetable
                                .getCourseOffering()
                                .getCourse()
                                .getName()
                )

                .facultyId(
                        timetable
                                .getFacultyAssignment()
                                .getFaculty()
                                .getId()
                )

                .facultyName(
                        timetable
                                .getFacultyAssignment()
                                .getFaculty()
                                .getFirstName()
                                + " "
                                + timetable
                                .getFacultyAssignment()
                                .getFaculty()
                                .getLastName()
                )

                .attendanceDate(
                        attendance.getAttendanceDate()
                )

                .status(
                        attendance.getStatus()
                )

                .remarks(
                        attendance.getRemarks()
                )

                .markedAt(
                        attendance.getMarkedAt()
                )

                .updatedAt(
                        attendance.getUpdatedAt()
                )

                .build();
    }
}