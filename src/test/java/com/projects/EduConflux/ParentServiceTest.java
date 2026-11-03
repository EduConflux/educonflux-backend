package com.projects.EduConflux;

import com.projects.EduConflux.attendance.entity.Attendance;
import com.projects.EduConflux.attendance.entity.AttendanceStatus;
import com.projects.EduConflux.attendance.repository.AttendanceRepository;
import com.projects.EduConflux.common.security.AuthenticatedUser;
import com.projects.EduConflux.identity.dto.UserResponse;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.identity.service.UserService;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.institution.repository.InstitutionRepository;
import com.projects.EduConflux.parent.dto.LinkStudentRequest;
import com.projects.EduConflux.parent.dto.ParentAttendanceSummaryResponse;
import com.projects.EduConflux.parent.dto.ParentRequest;
import com.projects.EduConflux.parent.dto.ParentResponse;
import com.projects.EduConflux.parent.entity.Parent;
import com.projects.EduConflux.parent.entity.StudentParent;
import com.projects.EduConflux.parent.repository.ParentRepository;
import com.projects.EduConflux.parent.repository.StudentParentRepository;
import com.projects.EduConflux.parent.service.ParentService;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParentServiceTest {

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private StudentParentRepository studentParentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private ParentService parentService;

    private Institution institution;
    private Student student;
    private User parentUser;
    private Parent parent;

    @BeforeEach
    void setUp() {
        institution = Institution.builder()
                .id(1L)
                .name("Springfield University")
                .build();

        student = Student.builder()
                .id(10L)
                .institution(institution)
                .firstName("Bart")
                .lastName("Simpson")
                .enrollmentNumber("STU-1001")
                .email("bart@springfield.edu")
                .build();

        parentUser = User.builder()
                .id(50L)
                .institution(institution)
                .firstName("Homer")
                .lastName("Simpson")
                .email("homer@springfield.edu")
                .build();

        parent = Parent.builder()
                .id(100L)
                .institution(institution)
                .user(parentUser)
                .occupation("Safety Inspector")
                .address("742 Evergreen Terrace")
                .build();

        AuthenticatedUser authUser = new AuthenticatedUser(
                50L,
                "homer@springfield.edu",
                1L,
                List.of("PARENT")
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(authUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("TC-PARENT-01: Successfully create parent and link initial student")
    void testCreateParent_Success() {
        ParentRequest request = new ParentRequest();
        request.setStudentId(10L);
        request.setFirstName("Homer");
        request.setLastName("Simpson");
        request.setEmail("homer@springfield.edu");
        request.setPersonalEmail("homer@gmail.com");
        request.setRelationship("Father");
        request.setOccupation("Safety Inspector");
        request.setAddress("742 Evergreen Terrace");

        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institution));
        when(studentRepository.findByIdAndInstitutionId(10L, 1L)).thenReturn(Optional.of(student));
        when(userRepository.findByInstitutionIdAndEmail(1L, "homer@springfield.edu")).thenReturn(Optional.empty());

        UserResponse userResponse = new UserResponse();
        userResponse.setId(50L);
        userResponse.setEmail("homer@springfield.edu");
        userResponse.setFirstName("Homer");
        userResponse.setLastName("Simpson");

        when(userService.createUser(any())).thenReturn(userResponse);
        when(userRepository.findByIdAndInstitutionId(50L, 1L)).thenReturn(Optional.of(parentUser));
        when(parentRepository.save(any(Parent.class))).thenReturn(parent);

        ParentResponse response = parentService.createParent(request);

        assertNotNull(response);
        assertEquals("Homer", response.getFirstName());
        assertEquals("Simpson", response.getLastName());
        assertEquals("Bart Simpson", response.getStudentName());
        assertEquals("Father", response.getRelationship());
        verify(studentParentRepository, times(1)).save(any(StudentParent.class));
    }

    @Test
    @DisplayName("TC-PARENT-02: Create parent fails when institutional email already exists")
    void testCreateParent_EmailAlreadyExists() {
        ParentRequest request = new ParentRequest();
        request.setStudentId(10L);
        request.setFirstName("Homer");
        request.setLastName("Simpson");
        request.setEmail("homer@springfield.edu");
        request.setPersonalEmail("homer@gmail.com");

        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institution));
        when(studentRepository.findByIdAndInstitutionId(10L, 1L)).thenReturn(Optional.of(student));
        when(userRepository.findByInstitutionIdAndEmail(1L, "homer@springfield.edu"))
                .thenReturn(Optional.of(parentUser));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> parentService.createParent(request));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("TC-PARENT-03: Successfully link another student (sibling) to parent")
    void testLinkStudent_Success() {
        Student sibling = Student.builder()
                .id(11L)
                .institution(institution)
                .firstName("Lisa")
                .lastName("Simpson")
                .enrollmentNumber("STU-1002")
                .build();

        LinkStudentRequest request = new LinkStudentRequest();
        request.setStudentId(11L);
        request.setRelationship("Father");

        when(parentRepository.findByIdAndInstitutionId(100L, 1L)).thenReturn(Optional.of(parent));
        when(studentRepository.findByIdAndInstitutionId(11L, 1L)).thenReturn(Optional.of(sibling));
        when(studentParentRepository.existsByParentIdAndStudentIdAndInstitutionId(100L, 11L, 1L))
                .thenReturn(false);

        ParentResponse response = parentService.linkStudent(100L, request);

        assertNotNull(response);
        assertEquals("Lisa Simpson", response.getStudentName());
        verify(studentParentRepository, times(1)).save(any(StudentParent.class));
    }

    @Test
    @DisplayName("TC-PARENT-04: Linking already linked student throws exception")
    void testLinkStudent_AlreadyLinkedThrowsException() {
        LinkStudentRequest request = new LinkStudentRequest();
        request.setStudentId(10L);
        request.setRelationship("Father");

        when(parentRepository.findByIdAndInstitutionId(100L, 1L)).thenReturn(Optional.of(parent));
        when(studentRepository.findByIdAndInstitutionId(10L, 1L)).thenReturn(Optional.of(student));
        when(studentParentRepository.existsByParentIdAndStudentIdAndInstitutionId(100L, 10L, 1L))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> parentService.linkStudent(100L, request));
        assertTrue(ex.getMessage().contains("already linked"));
    }

    @Test
    @DisplayName("TC-PARENT-05: Parent attendance summary calculates percentages correctly")
    void testGetStudentAttendanceSummary_CalculatesCorrectly() {
        when(parentRepository.findByUserIdAndInstitutionId(50L, 1L)).thenReturn(Optional.of(parent));
        when(studentRepository.findByIdAndInstitutionId(10L, 1L)).thenReturn(Optional.of(student));
        when(studentParentRepository.existsByParentIdAndStudentIdAndInstitutionId(100L, 10L, 1L)).thenReturn(true);

        List<Attendance> mockRecords = List.of(
                Attendance.builder().status(AttendanceStatus.PRESENT).build(),
                Attendance.builder().status(AttendanceStatus.PRESENT).build(),
                Attendance.builder().status(AttendanceStatus.PRESENT).build(),
                Attendance.builder().status(AttendanceStatus.LATE).build(),
                Attendance.builder().status(AttendanceStatus.ABSENT).build()
        );
        when(attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(10L)).thenReturn(mockRecords);

        ParentAttendanceSummaryResponse summary = parentService.getStudentAttendanceSummary(10L);

        assertNotNull(summary);
        assertEquals(5, summary.getTotalClasses());
        assertEquals(3, summary.getPresent());
        assertEquals(1, summary.getLate());
        assertEquals(1, summary.getAbsent());
        assertEquals(80.0, summary.getAttendancePercentage(), 0.01);
    }
}
