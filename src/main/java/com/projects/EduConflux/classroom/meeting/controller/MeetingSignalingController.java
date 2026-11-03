package com.projects.EduConflux.classroom.meeting.controller;

import com.projects.EduConflux.classroom.meeting.dto.MeetingParticipantResponse;
import com.projects.EduConflux.classroom.meeting.dto.SignalMessageType;
import com.projects.EduConflux.classroom.meeting.dto.WebRTCSignalMessage;
import com.projects.EduConflux.classroom.meeting.service.ClassroomMeetingService;
import com.projects.EduConflux.common.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MeetingSignalingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ClassroomMeetingService meetingService;

    /**
     * Peer joins the classroom meeting.
     * Client sends: /app/classroom/{classroomId}/meeting/join
     * Server broadcasts to: /topic/classroom/{classroomId}/meeting/peers
     */
    @MessageMapping("/classroom/{classroomId}/meeting/join")
    public void handleJoinMeeting(
            @DestinationVariable Long classroomId,
            Principal principal) {

        AuthenticatedUser user = extractUser(principal);
        log.info("User {} ({}) joined meeting in classroom {}", user.getUserId(), user.getEmail(), classroomId);

        MeetingParticipantResponse participant = meetingService.joinMeeting(classroomId, user.getUserId());

        WebRTCSignalMessage signal = WebRTCSignalMessage.builder()
                .type(SignalMessageType.PEER_JOINED)
                .senderId(participant.getUserId())
                .senderName(participant.getUserName())
                .senderRole(participant.getRole() != null ? participant.getRole().name() : "PARTICIPANT")
                .timestamp(LocalDateTime.now().toString())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/classroom/" + classroomId + "/meeting/peers",
                signal
        );
    }

    /**
     * WebRTC SDP Offer / Answer relay to a specific target peer.
     * Client sends: /app/classroom/{classroomId}/meeting/signal/{recipientId}
     * Server relays to: /topic/classroom/{classroomId}/meeting/signal/{recipientId}
     */
    @MessageMapping("/classroom/{classroomId}/meeting/signal/{recipientId}")
    public void handleSignalRelay(
            @DestinationVariable Long classroomId,
            @DestinationVariable Long recipientId,
            @Payload WebRTCSignalMessage message,
            Principal principal) {

        AuthenticatedUser user = extractUser(principal);
        message.setSenderId(user.getUserId());
        message.setTargetUserId(recipientId);

        messagingTemplate.convertAndSend(
                "/topic/classroom/" + classroomId + "/meeting/signal/" + recipientId,
                message
        );
    }

    /**
     * Trickle ICE candidate relay to a specific target peer.
     * Client sends: /app/classroom/{classroomId}/meeting/ice/{recipientId}
     * Server relays to: /topic/classroom/{classroomId}/meeting/ice/{recipientId}
     */
    @MessageMapping("/classroom/{classroomId}/meeting/ice/{recipientId}")
    public void handleIceCandidateRelay(
            @DestinationVariable Long classroomId,
            @DestinationVariable Long recipientId,
            @Payload WebRTCSignalMessage message,
            Principal principal) {

        AuthenticatedUser user = extractUser(principal);
        message.setSenderId(user.getUserId());
        message.setTargetUserId(recipientId);

        messagingTemplate.convertAndSend(
                "/topic/classroom/" + classroomId + "/meeting/ice/" + recipientId,
                message
        );
    }

    /**
     * Participant updates media state (mic, cam, screen, hand raise).
     * Client sends: /app/classroom/{classroomId}/meeting/state
     * Server broadcasts to: /topic/classroom/{classroomId}/meeting/state
     */
    @MessageMapping("/classroom/{classroomId}/meeting/state")
    public void handleMediaStateChange(
            @DestinationVariable Long classroomId,
            @Payload WebRTCSignalMessage message,
            Principal principal) {

        AuthenticatedUser user = extractUser(principal);
        message.setSenderId(user.getUserId());

        meetingService.updateParticipantMediaState(
                classroomId,
                user.getUserId(),
                message.getIsAudioMuted(),
                message.getIsVideoOff(),
                message.getIsHandRaised()
        );

        messagingTemplate.convertAndSend(
                "/topic/classroom/" + classroomId + "/meeting/state",
                message
        );
    }

    /**
     * Host controls (Mute All, Kick, End Meeting).
     * Client sends: /app/classroom/{classroomId}/meeting/control
     * Server broadcasts to: /topic/classroom/{classroomId}/meeting/control
     */
    @MessageMapping("/classroom/{classroomId}/meeting/control")
    public void handleHostControl(
            @DestinationVariable Long classroomId,
            @Payload WebRTCSignalMessage message,
            Principal principal) {

        AuthenticatedUser user = extractUser(principal);
        message.setSenderId(user.getUserId());

        messagingTemplate.convertAndSend(
                "/topic/classroom/" + classroomId + "/meeting/control",
                message
        );
    }

    /**
     * Peer leaves the classroom meeting.
     * Client sends: /app/classroom/{classroomId}/meeting/leave
     * Server broadcasts to: /topic/classroom/{classroomId}/meeting/peers
     */
    @MessageMapping("/classroom/{classroomId}/meeting/leave")
    public void handleLeaveMeeting(
            @DestinationVariable Long classroomId,
            Principal principal) {

        AuthenticatedUser user = extractUser(principal);
        log.info("User {} left meeting in classroom {}", user.getUserId(), classroomId);

        meetingService.leaveMeeting(classroomId, user.getUserId());

        WebRTCSignalMessage signal = WebRTCSignalMessage.builder()
                .type(SignalMessageType.PEER_LEFT)
                .senderId(user.getUserId())
                .timestamp(LocalDateTime.now().toString())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/classroom/" + classroomId + "/meeting/peers",
                signal
        );
    }

    private AuthenticatedUser extractUser(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Unauthenticated WebSocket session");
        }
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new IllegalStateException("Invalid WebSocket principal type: " + principal.getClass().getName());
    }
}

