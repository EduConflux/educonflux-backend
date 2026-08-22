package com.projects.EduConflux.timetable.service;

import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import com.projects.EduConflux.timetable.dto.TimetableEntryResponse;
import com.projects.EduConflux.timetable.entity.TimetableEntry;
import com.projects.EduConflux.timetable.repository.TimetableEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableDashboardService {

    private final TimetableEntryRepository timetableRepository;

    private final StudentRepository studentRepository;

    private final FacultyRepository facultyRepository;


    /*
     * STUDENT
     * Today's timetable
     */
    public List<TimetableEntryResponse> getStudentToday() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String email =
                SecurityUtils.getCurrentUserEmail();

        Student student =
                studentRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                email
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student profile not found"
                                ));


        if (student.getClassSection() == null) {

            throw new RuntimeException(
                    "Student is not assigned to a class section"
            );
        }


        DayOfWeek today =
                LocalDate.now().getDayOfWeek();


        return timetableRepository
                .findByInstitutionIdAndClassSectionIdAndDayOfWeek(
                        institutionId,
                        student.getClassSection().getId(),
                        today
                )
                .stream()
                .filter(entry ->
                        entry.getStatus()
                                == com.projects.EduConflux.timetable.entity.TimetableStatus.ACTIVE
                )
                .sorted(
                        java.util.Comparator.comparing(
                                TimetableEntry::getStartTime
                        )
                )
                .map(this::toResponse)
                .toList();
    }


    /*
     * STUDENT
     * Complete weekly timetable
     */
    public List<TimetableEntryResponse> getStudentWeekly() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String email =
                SecurityUtils.getCurrentUserEmail();

        Student student =
                studentRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                email
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student profile not found"
                                ));


        if (student.getClassSection() == null) {

            throw new RuntimeException(
                    "Student is not assigned to a class section"
            );
        }


        return timetableRepository
                .findByInstitutionIdAndClassSectionId(
                        institutionId,
                        student.getClassSection().getId()
                )
                .stream()
                .filter(entry ->
                        entry.getStatus()
                                == com.projects.EduConflux.timetable.entity.TimetableStatus.ACTIVE
                )
                .sorted(
                        java.util.Comparator
                                .comparing(TimetableEntry::getDayOfWeek)
                                .thenComparing(
                                        TimetableEntry::getStartTime
                                )
                )
                .map(this::toResponse)
                .toList();
    }


    /*
     * FACULTY
     * Today's timetable
     */
    public List<TimetableEntryResponse> getFacultyToday() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String email =
                SecurityUtils.getCurrentUserEmail();

        Faculty faculty =
                facultyRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                email
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty profile not found"
                                ));


        DayOfWeek today =
                LocalDate.now().getDayOfWeek();


        return timetableRepository
                .findByInstitutionIdAndFacultyAssignmentFacultyIdAndDayOfWeek(
                        institutionId,
                        faculty.getId(),
                        today
                )
                .stream()
                .filter(entry ->
                        entry.getStatus()
                                == com.projects.EduConflux.timetable.entity.TimetableStatus.ACTIVE
                )
                .sorted(
                        java.util.Comparator.comparing(
                                TimetableEntry::getStartTime
                        )
                )
                .map(this::toResponse)
                .toList();
    }


    /*
     * FACULTY
     * Complete weekly timetable
     */
    public List<TimetableEntryResponse> getFacultyWeekly() {

        Long institutionId =
                SecurityUtils.getCurrentInstitutionId();

        String email =
                SecurityUtils.getCurrentUserEmail();

        Faculty faculty =
                facultyRepository
                        .findByInstitutionIdAndEmail(
                                institutionId,
                                email
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty profile not found"
                                ));


        return timetableRepository
                .findByInstitutionIdAndFacultyAssignmentFacultyId(
                        institutionId,
                        faculty.getId()
                )
                .stream()
                .filter(entry ->
                        entry.getStatus()
                                == com.projects.EduConflux.timetable.entity.TimetableStatus.ACTIVE
                )
                .sorted(
                        java.util.Comparator
                                .comparing(TimetableEntry::getDayOfWeek)
                                .thenComparing(
                                        TimetableEntry::getStartTime
                                )
                )
                .map(this::toResponse)
                .toList();
    }


    /*
     * Convert entity → response.
     *
     * We reuse the same response structure
     * already used by the admin timetable.
     */
    private TimetableEntryResponse toResponse(
            TimetableEntry entry) {

        TimetableEntryResponse response =
                new TimetableEntryResponse();


        response.setId(
                entry.getId()
        );


        response.setInstitutionId(
                entry.getInstitution().getId()
        );


        response.setClassSectionId(
                entry.getClassSection().getId()
        );

        response.setClassSectionName(
                entry.getClassSection().getName()
        );


        response.setCourseOfferingId(
                entry.getCourseOffering().getId()
        );


        response.setCourseId(
                entry.getCourseOffering()
                        .getCourse()
                        .getId()
        );

        response.setCourseCode(
                entry.getCourseOffering()
                        .getCourse()
                        .getCode()
        );

        response.setCourseName(
                entry.getCourseOffering()
                        .getCourse()
                        .getName()
        );


        response.setFacultyAssignmentId(
                entry.getFacultyAssignment().getId()
        );


        response.setFacultyId(
                entry.getFacultyAssignment()
                        .getFaculty()
                        .getId()
        );

        response.setFacultyName(
                entry.getFacultyAssignment()
                        .getFaculty()
                        .getFirstName()
                        + " "
                        + entry.getFacultyAssignment()
                        .getFaculty()
                        .getLastName()
        );

        response.setFacultyEmployeeId(
                entry.getFacultyAssignment()
                        .getFaculty()
                        .getEmployeeId()
        );


        response.setProgramId(
                entry.getCourseOffering()
                        .getCourse()
                        .getProgram()
                        .getId()
        );

        response.setProgramName(
                entry.getCourseOffering()
                        .getCourse()
                        .getProgram()
                        .getName()
        );


        response.setSemesterId(
                entry.getCourseOffering()
                        .getCourse()
                        .getSemester()
                        .getId()
        );

        response.setSemesterNumber(
                entry.getCourseOffering()
                        .getCourse()
                        .getSemester()
                        .getSemesterNumber()
        );

        response.setSemesterName(
                entry.getCourseOffering()
                        .getCourse()
                        .getSemester()
                        .getName()
        );


        response.setAcademicYearId(
                entry.getCourseOffering()
                        .getAcademicYear()
                        .getId()
        );

        response.setAcademicYearName(
                entry.getCourseOffering()
                        .getAcademicYear()
                        .getName()
        );


        response.setDayOfWeek(
                entry.getDayOfWeek()
        );

        response.setStartTime(
                entry.getStartTime()
        );

        response.setEndTime(
                entry.getEndTime()
        );

        response.setRoom(
                entry.getRoom()
        );

        response.setStatus(
                entry.getStatus()
        );

        response.setCreatedAt(
                entry.getCreatedAt()
        );

        response.setUpdatedAt(
                entry.getUpdatedAt()
        );


        return response;
    }
}