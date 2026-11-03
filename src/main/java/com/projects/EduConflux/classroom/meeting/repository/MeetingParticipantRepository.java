package com.projects.EduConflux.classroom.meeting.repository;

import com.projects.EduConflux.classroom.meeting.entity.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    List<MeetingParticipant> findByMeetingIdOrderByJoinedAtAsc(Long meetingId);

    List<MeetingParticipant> findByMeetingIdAndLeftAtIsNull(Long meetingId);

    Optional<MeetingParticipant> findByMeetingIdAndUserIdAndLeftAtIsNull(Long meetingId, Long userId);

    Optional<MeetingParticipant> findTopByMeetingIdAndUserIdOrderByJoinedAtDesc(Long meetingId, Long userId);

    long countByMeetingIdAndLeftAtIsNull(Long meetingId);
}

