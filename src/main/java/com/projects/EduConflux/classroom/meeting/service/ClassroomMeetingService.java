package com.projects.EduConflux.classroom.meeting.service;

import com.projects.EduConflux.classroom.entity.Classroom;
import com.projects.EduConflux.classroom.meeting.dto.*;
import com.projects.EduConflux.classroom.meeting.entity.ClassroomMeeting;
import com.projects.EduConflux.classroom.meeting.entity.MeetingParticipant;
import com.projects.EduConflux.classroom.meeting.entity.MeetingStatus;
import com.projects.EduConflux.classroom.meeting.entity.ParticipantRole;
import com.projects.EduConflux.classroom.meeting.repository.ClassroomMeetingRepository;
import com.projects.EduConflux.classroom.meeting.repository.MeetingParticipantRepository;
import com.projects.EduConflux.classroom.membership.repository.ClassroomMembershipRepository;
import com.projects.EduConflux.classroom.repository.ClassroomRepository;
import com.projects.EduConflux.common.security.SecurityUtils;
import com.projects.EduConflux.faculty.entity.Faculty;
import com.projects.EduConflux.faculty.repository.FacultyRepository;
import com.projects.EduConflux.identity.entity.User;
import com.projects.EduConflux.identity.repository.UserRepository;
import com.projects.EduConflux.student.entity.Student;
import com.projects.EduConflux.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassroomMeetingService {

    private final ClassroomMeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomMembershipRepository membershipRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClassroomMeetingResponse startMeeting(Long classroomId, CreateMeetingRequest request) {
        Long institutionId = SecurityUtils.getCurrentInstitutionId();
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Classroom classroom = classroomRepository.findByIdAndInstitutionId(classroomId, institutionId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        User hostUser = userRepository.findByIdAndInstitutionId(currentUserId, institutionId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        // Verify caller is the classroom faculty instructor or institution admin
        boolean isAdmin = SecurityUtils.hasRole("INSTITUTION_ADMIN");
        boolean isClassroomFaculty = classroom.getFaculty().getEmail().equalsIgnoreCase(hostUser.getEmail());

        if (!isAdmin && !isClassroomFaculty) {
            throw new RuntimeException("Only the classroom instructor or an admin can start a video meeting");
        }

        // If an active meeting already exists, return that active meeting
        Optional<ClassroomMeeting> existingActive = meetingRepository.findByClassroomIdAndStatus(classroomId, MeetingStatus.ACTIVE);
        if (existingActive.isPresent()) {
            return toMeetingResponse(existingActive.get());
        }

        String roomCode = "clr-" + classroomId + "-m-" + UUID.randomUUID().toString().substring(0, 8);

        ClassroomMeeting meeting = ClassroomMeeting.builder()
                .classroom(classroom)
                .hostUser(hostUser)
                .title(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle() : classroom.getName() + " Live Session")
                .status(MeetingStatus.ACTIVE)
                .roomCode(roomCode)
                .scheduledStartTime(request.getScheduledStartTime())
                .startedAt(LocalDateTime.now())
                .allowStudentUnmute(request.isAllowStudentUnmute())
                .allowStudentScreenShare(request.isAllowStudentScreenShare())
                .build();

        ClassroomMeeting savedMeeting = meetingRepository.save(meeting);

        // Add host as the initial participant
        MeetingParticipant hostParticipant = MeetingParticipant.builder()
                .meeting(savedMeeting)
                .user(hostUser)
                .role(ParticipantRole.HOST)
                .joinedAt(LocalDateTime.now())
                .build();
        participantRepository.save(hostParticipant);

        return toMeetingResponse(savedMeeting);
    }

    @Transactional(readOnly = true)
    public Optional<ClassroomMeetingResponse> getActiveMeeting(Long classroomId) {
        Long institutionId = SecurityUtils.getCurrentInstitutionId();
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Classroom classroom = classroomRepository.findByIdAndInstitutionId(classroomId, institutionId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        validateClassroomAccess(classroom, currentUserId, institutionId);

        return meetingRepository.findByClassroomIdAndStatus(classroomId, MeetingStatus.ACTIVE)
                .map(this::toMeetingResponse);
    }

    @Transactional
    public ClassroomMeetingResponse endMeeting(Long classroomId, Long meetingId) {
        Long institutionId = SecurityUtils.getCurrentInstitutionId();
        Long currentUserId = SecurityUtils.getCurrentUserId();

        ClassroomMeeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        if (!meeting.getClassroom().getId().equals(classroomId)) {
            throw new RuntimeException("Meeting does not belong to this classroom");
        }

        boolean isAdmin = SecurityUtils.hasRole("INSTITUTION_ADMIN");
        boolean isHost = meeting.getHostUser().getId().equals(currentUserId);
        boolean isClassroomFaculty = meeting.getClassroom().getFaculty().getEmail().equalsIgnoreCase(SecurityUtils.getCurrentUserEmail());

        if (!isAdmin && !isHost && !isClassroomFaculty) {
            throw new RuntimeException("You do not have permission to end this meeting");
        }

        LocalDateTime now = LocalDateTime.now();
        meeting.setStatus(MeetingStatus.ENDED);
        meeting.setEndedAt(now);
        ClassroomMeeting endedMeeting = meetingRepository.save(meeting);

        // Close any active participants
        List<MeetingParticipant> openParticipants = participantRepository.findByMeetingIdAndLeftAtIsNull(meetingId);
        for (MeetingParticipant p : openParticipants) {
            p.setLeftAt(now);
            if (p.getJoinedAt() != null) {
                p.setDurationSeconds((int) Duration.between(p.getJoinedAt(), now).toSeconds());
            }
            participantRepository.save(p);
        }

        return toMeetingResponse(endedMeeting);
    }

    @Transactional
    public MeetingParticipantResponse joinMeeting(Long classroomId, Long userId) {
        ClassroomMeeting meeting = meetingRepository.findByClassroomIdAndStatus(classroomId, MeetingStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active meeting in this classroom"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateClassroomAccess(meeting.getClassroom(), userId, user.getInstitution().getId());

        // Check if user is already an open participant
        Optional<MeetingParticipant> existing = participantRepository.findByMeetingIdAndUserIdAndLeftAtIsNull(meeting.getId(), userId);
        if (existing.isPresent()) {
            return toParticipantResponse(existing.get());
        }

        boolean isHost = meeting.getHostUser().getId().equals(userId)
                || meeting.getClassroom().getFaculty().getEmail().equalsIgnoreCase(user.getEmail());

        MeetingParticipant participant = MeetingParticipant.builder()
                .meeting(meeting)
                .user(user)
                .role(isHost ? ParticipantRole.HOST : ParticipantRole.PARTICIPANT)
                .joinedAt(LocalDateTime.now())
                .micMuted(false)
                .cameraOff(false)
                .handRaised(false)
                .build();

        MeetingParticipant saved = participantRepository.save(participant);
        return toParticipantResponse(saved);
    }

    @Transactional
    public void leaveMeeting(Long classroomId, Long userId) {
        Optional<ClassroomMeeting> meetingOpt = meetingRepository.findByClassroomIdAndStatus(classroomId, MeetingStatus.ACTIVE);
        if (meetingOpt.isEmpty()) {
            return;
        }

        ClassroomMeeting meeting = meetingOpt.get();
        Optional<MeetingParticipant> participantOpt = participantRepository.findByMeetingIdAndUserIdAndLeftAtIsNull(meeting.getId(), userId);
        if (participantOpt.isPresent()) {
            MeetingParticipant p = participantOpt.get();
            LocalDateTime now = LocalDateTime.now();
            p.setLeftAt(now);
            if (p.getJoinedAt() != null) {
                p.setDurationSeconds((int) Duration.between(p.getJoinedAt(), now).toSeconds());
            }
            participantRepository.save(p);
        }
    }

    @Transactional(readOnly = true)
    public List<MeetingParticipantResponse> getParticipants(Long meetingId) {
        return participantRepository.findByMeetingIdOrderByJoinedAtAsc(meetingId).stream()
                .map(this::toParticipantResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClassroomMeetingResponse> getMeetingHistory(Long classroomId) {
        return meetingRepository.findByClassroomIdOrderByCreatedAtDesc(classroomId).stream()
                .map(this::toMeetingResponse)
                .toList();
    }

    @Transactional
    public void updateParticipantMediaState(Long classroomId, Long userId, Boolean isMuted, Boolean isVideoOff, Boolean isHandRaised) {
        Optional<ClassroomMeeting> meetingOpt = meetingRepository.findByClassroomIdAndStatus(classroomId, MeetingStatus.ACTIVE);
        if (meetingOpt.isEmpty()) return;

        Optional<MeetingParticipant> participantOpt = participantRepository.findByMeetingIdAndUserIdAndLeftAtIsNull(meetingOpt.get().getId(), userId);
        if (participantOpt.isPresent()) {
            MeetingParticipant p = participantOpt.get();
            if (isMuted != null) p.setMicMuted(isMuted);
            if (isVideoOff != null) p.setCameraOff(isVideoOff);
            if (isHandRaised != null) p.setHandRaised(isHandRaised);
            participantRepository.save(p);
        }
    }

    public List<IceServerResponse> getIceServers() {
        return List.of(
                IceServerResponse.builder()
                        .urls(List.of(
                                "stun:stun.l.google.com:19302",
                                "stun:stun1.l.google.com:19302",
                                "stun:stun2.l.google.com:19302",
                                "stun:stun3.l.google.com:19302",
                                "stun:stun4.l.google.com:19302"
                        ))
                        .build()
        );
    }

    private void validateClassroomAccess(Classroom classroom, Long userId, Long institutionId) {
        if (SecurityUtils.hasRole("INSTITUTION_ADMIN")) {
            return;
        }

        // Check if instructor or student
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            if (classroom.getFaculty().getEmail().equalsIgnoreCase(user.getEmail())) {
                return;
            }
            Optional<Student> studentOpt = studentRepository.findByInstitutionIdAndEmail(institutionId, user.getEmail());
            if (studentOpt.isPresent()) {
                boolean isEnrolled = membershipRepository.existsByClassroomIdAndStudentId(classroom.getId(), studentOpt.get().getId());
                if (isEnrolled) {
                    return;
                }
            }
        }

        throw new RuntimeException("Access denied: You are not an enrolled member of this classroom");
    }

    private ClassroomMeetingResponse toMeetingResponse(ClassroomMeeting m) {
        long activeParticipants = participantRepository.countByMeetingIdAndLeftAtIsNull(m.getId());

        String hostFullName = m.getHostUser() != null
                ? m.getHostUser().getFirstName() + " " + (m.getHostUser().getLastName() != null ? m.getHostUser().getLastName() : "")
                : "Host";

        return ClassroomMeetingResponse.builder()
                .id(m.getId())
                .classroomId(m.getClassroom().getId())
                .classroomName(m.getClassroom().getName())
                .hostUserId(m.getHostUser().getId())
                .hostName(hostFullName.trim())
                .title(m.getTitle())
                .status(m.getStatus())
                .roomCode(m.getRoomCode())
                .scheduledStartTime(m.getScheduledStartTime())
                .startedAt(m.getStartedAt())
                .endedAt(m.getEndedAt())
                .allowStudentUnmute(m.isAllowStudentUnmute())
                .allowStudentScreenShare(m.isAllowStudentScreenShare())
                .activeParticipantCount(activeParticipants)
                .createdAt(m.getCreatedAt())
                .build();
    }

    private MeetingParticipantResponse toParticipantResponse(MeetingParticipant p) {
        String name = p.getUser() != null
                ? p.getUser().getFirstName() + " " + (p.getUser().getLastName() != null ? p.getUser().getLastName() : "")
                : "Participant";

        return MeetingParticipantResponse.builder()
                .id(p.getId())
                .meetingId(p.getMeeting().getId())
                .userId(p.getUser().getId())
                .userName(name.trim())
                .userEmail(p.getUser() != null ? p.getUser().getEmail() : "")
                .role(p.getRole())
                .joinedAt(p.getJoinedAt())
                .leftAt(p.getLeftAt())
                .durationSeconds(p.getDurationSeconds())
                .handRaised(p.isHandRaised())
                .micMuted(p.isMicMuted())
                .cameraOff(p.isCameraOff())
                .build();
    }
}
