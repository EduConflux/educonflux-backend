package com.projects.EduConflux;

import com.projects.EduConflux.attendance.repository.AttendanceRepository;
import com.projects.EduConflux.attendance.service.AttendanceService;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.student.repository.StudentRepository;
import com.projects.EduConflux.timetable.repository.TimetableEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private FacultyRepository facultyRepository;
    @Mock
    private TimetableEntryRepository timetableEntryRepository;

    @InjectMocks
    private AttendanceService service;

    @Test
    void testCalculatePercentage_NormalCase() {
        int attended = 8;
        int total = 10;

        double result = service.calculatePercentage(attended, total);

        assertEquals(80.0, result, 0.01);
    }

    @Test
    void testCalculatePercentage_ZeroAttended() {
        double result = service.calculatePercentage(0, 10);
        assertEquals(0.0, result, 0.01);
    }

    @Test
    void testCalculatePercentage_ZeroTotalClasses() {
        double result = service.calculatePercentage(0, 0);
        assertEquals(0.0, result, 0.01);
    }


    @Test
    void testisEligibleForExam_ExactlySeventyFivePercent(){
        boolean eligible = service.isEligibleForExam(30,40);
        assertTrue(eligible,"Student with exactly 75% attendence must be eligible for exams");
    }

    @Test
    void testIsEligibleForExam_BelowSeventyFivePercent() {
        boolean eligible = service.isEligibleForExam(29, 40); // 29/40 = 72.5%
        assertFalse(eligible, "Student below 75% attendance must NOT be eligible for exams");
    }

    @Test
    void testIsEligibleForExam_FullAttendance() {
        boolean eligible = service.isEligibleForExam(40, 40); // 100%
        assertTrue(eligible);
    }
}
