package com.projects.EduConflux.classroom.meeting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebRTCSignalMessage {

    private SignalMessageType type;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private Long targetUserId;

    // SDP Offer / Answer payload
    private Map<String, Object> sdp;

    // Trickle ICE candidate payload
    private Map<String, Object> candidate;

    // Media states (mic, cam, screen, hand)
    private Boolean isAudioMuted;
    private Boolean isVideoOff;
    private Boolean isScreenSharing;
    private Boolean isHandRaised;

    // Host commands (e.g. MUTE_AUDIO, KICK, END_MEETING)
    private String hostCommand;

    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();
}

