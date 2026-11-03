package com.projects.EduConflux.classroom.meeting.repository;

import com.projects.EduConflux.classroom.meeting.entity.ClassroomMeeting;
import com.projects.EduConflux.classroom.meeting.entity.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomMeetingRepository extends JpaRepository<ClassroomMeeting, Long> {

    Optional<ClassroomMeeting> findByClassroomIdAndStatus(Long classroomId, MeetingStatus status);

    Optional<ClassroomMeeting> findByRoomCode(String roomCode);

    List<ClassroomMeeting> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);

    boolean existsByClassroomIdAndStatus(Long classroomId, MeetingStatus status);
}

