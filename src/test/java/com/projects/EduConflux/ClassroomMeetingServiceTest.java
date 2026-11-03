package com.projects.EduConflux;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.meeting.dto.ClassroomMeetingResponse;
import com.projects.EduConflux.classroom.meeting.dto.CreateMeetingRequest;
import com.projects.EduConflux.classroom.meeting.dto.MeetingParticipantResponse;
import com.projects.EduConflux.classroom.meeting.entity.ClassroomMeeting;
import com.projects.EduConflux.classroom.meeting.entity.MeetingParticipant;
import com.projects.EduConflux.classroom.meeting.entity.MeetingStatus;
import com.projects.EduConflux.classroom.meeting.entity.ParticipantRole;
import com.projects.EduConflux.classroom.meeting.repository.ClassroomMeetingRepository;
import com.projects.EduConflux.classroom.meeting.repository.MeetingParticipantRepository;
import com.projects.EduConflux.classroom.membership.repository.ClassroomMembershipRepository;
import com.projects.EduConflux.classroom.repository.ClassroomRepository;
import com.projects.EduConflux.common.security.AuthenticatedUser;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.institution.entity.Institution;
import com.projects.EduConflux.classroom.meeting.service.ClassroomMeetingService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClassroomMeetingServiceTest {

    @Mock
    private ClassroomMeetingRepository meetingRepository;

    @Mock
    private MeetingParticipantRepository participantRepository;

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private ClassroomMembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ClassroomMeetingService meetingService;

    private Institution institution;
    private User facultyUser;
    private Faculty faculty;
    private Classroom classroom;
    private ClassroomMeeting meeting;

    @BeforeEach
    void setUp() {
        institution = Institution.builder().id(1L).name("Tech University").build();

        facultyUser = User.builder()
                .id(20L)
                .institution(institution)
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@tech.edu")
                .build();

        faculty = Faculty.builder()
                .id(5L)
                .institution(institution)
                .email("ada@tech.edu")
                .firstName("Ada")
                .lastName("Lovelace")
                .build();

        classroom = Classroom.builder()
                .id(40L)
                .institution(institution)
                .faculty(faculty)
                .name("CS101 Intro to Algorithms")
                .build();

        meeting = ClassroomMeeting.builder()
                .id(1L)
                .classroom(classroom)
                .hostUser(facultyUser)
                .title("Algorithms Live Session")
                .status(MeetingStatus.ACTIVE)
                .roomCode("clr-40-m-12345678")
                .startedAt(LocalDateTime.now())
                .allowStudentUnmute(true)
                .allowStudentScreenShare(false)
                .build();

        AuthenticatedUser authUser = new AuthenticatedUser(
                20L,
                "ada@tech.edu",
                1L,
                List.of("FACULTY")
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
    @DisplayName("TC-MEETING-01: Faculty successfully starts a new video lecture meeting")
    void testStartMeeting_Success() {
        CreateMeetingRequest request = CreateMeetingRequest.builder()
                .title("Algorithms Live Session")
                .allowStudentUnmute(true)
                .allowStudentScreenShare(false)
                .build();

        when(classroomRepository.findByIdAndInstitutionId(40L, 1L)).thenReturn(Optional.of(classroom));
        when(userRepository.findByIdAndInstitutionId(20L, 1L)).thenReturn(Optional.of(facultyUser));
        when(meetingRepository.findByClassroomIdAndStatus(40L, MeetingStatus.ACTIVE)).thenReturn(Optional.empty());
        when(meetingRepository.save(any(ClassroomMeeting.class))).thenReturn(meeting);

        ClassroomMeetingResponse response = meetingService.startMeeting(40L, request);

        assertNotNull(response);
        assertEquals(MeetingStatus.ACTIVE, response.getStatus());
        assertEquals("Algorithms Live Session", response.getTitle());
        assertTrue(response.getRoomCode().startsWith("clr-40-m-"));
        verify(participantRepository, times(1)).save(any(MeetingParticipant.class));
    }

    @Test
    @DisplayName("TC-MEETING-02: Returns existing meeting if already active in classroom")
    void testStartMeeting_ReturnsExistingIfAlreadyActive() {
        CreateMeetingRequest request = CreateMeetingRequest.builder().title("Second Call").build();

        when(classroomRepository.findByIdAndInstitutionId(40L, 1L)).thenReturn(Optional.of(classroom));
        when(userRepository.findByIdAndInstitutionId(20L, 1L)).thenReturn(Optional.of(facultyUser));
        when(meetingRepository.findByClassroomIdAndStatus(40L, MeetingStatus.ACTIVE)).thenReturn(Optional.of(meeting));

        ClassroomMeetingResponse response = meetingService.startMeeting(40L, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(meetingRepository, never()).save(any(ClassroomMeeting.class));
    }

    @Test
    @DisplayName("TC-MEETING-03: Unauthorized user cannot start video lecture")
    void testStartMeeting_DeniedForNonInstructor() {
        // Change auth context to an unrelated student
        AuthenticatedUser studentAuth = new AuthenticatedUser(99L, "student@tech.edu", 1L, List.of("STUDENT"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(studentAuth, null, List.of())
        );

        User studentUser = User.builder().id(99L).institution(institution).email("student@tech.edu").build();
        when(classroomRepository.findByIdAndInstitutionId(40L, 1L)).thenReturn(Optional.of(classroom));
        when(userRepository.findByIdAndInstitutionId(99L, 1L)).thenReturn(Optional.of(studentUser));

        CreateMeetingRequest request = CreateMeetingRequest.builder().title("Student Meeting").build();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> meetingService.startMeeting(40L, request));
        assertTrue(ex.getMessage().contains("Only the classroom instructor or an admin"));
    }

    @Test
    @DisplayName("TC-MEETING-04: Host concludes meeting and ends active participant sessions")
    void testEndMeeting_Success() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        MeetingParticipant activeParticipant = MeetingParticipant.builder()
                .id(10L)
                .meeting(meeting)
                .user(facultyUser)
                .joinedAt(LocalDateTime.now().minusMinutes(45))
                .build();
        when(participantRepository.findByMeetingIdAndLeftAtIsNull(1L)).thenReturn(List.of(activeParticipant));
        when(meetingRepository.save(any(ClassroomMeeting.class))).thenReturn(meeting);

        ClassroomMeetingResponse response = meetingService.endMeeting(40L, 1L);

        assertNotNull(response);
        assertEquals(MeetingStatus.ENDED, meeting.getStatus());
        assertNotNull(meeting.getEndedAt());
        verify(participantRepository, times(1)).save(activeParticipant);
        assertNotNull(activeParticipant.getLeftAt());
        assertTrue(activeParticipant.getDurationSeconds() > 0);
    }

    @Test
    @DisplayName("TC-MEETING-05: Joining inactive meeting throws exception")
    void testJoinMeeting_ThrowsWhenNoActiveMeeting() {
        when(meetingRepository.findByClassroomIdAndStatus(40L, MeetingStatus.ACTIVE)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> meetingService.joinMeeting(40L, 20L));
        assertTrue(ex.getMessage().contains("No active meeting"));
    }

    @Test
    @DisplayName("TC-MEETING-06: Participant joins active meeting and assigns HOST role correctly")
    void testJoinMeeting_Success() {
        when(meetingRepository.findByClassroomIdAndStatus(40L, MeetingStatus.ACTIVE)).thenReturn(Optional.of(meeting));
        when(userRepository.findById(20L)).thenReturn(Optional.of(facultyUser));
        when(participantRepository.findByMeetingIdAndUserIdAndLeftAtIsNull(1L, 20L)).thenReturn(Optional.empty());

        MeetingParticipant savedParticipant = MeetingParticipant.builder()
                .id(55L)
                .meeting(meeting)
                .user(facultyUser)
                .role(ParticipantRole.HOST)
                .joinedAt(LocalDateTime.now())
                .build();
        when(participantRepository.save(any(MeetingParticipant.class))).thenReturn(savedParticipant);

        MeetingParticipantResponse response = meetingService.joinMeeting(40L, 20L);

        assertNotNull(response);
        assertEquals(ParticipantRole.HOST, response.getRole());
        assertEquals("Ada Lovelace", response.getUserName());
    }
}

