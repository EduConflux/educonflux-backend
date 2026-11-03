package com.projects.EduConflux.classroom.meeting.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IceServerResponse {

    private List<String> urls;
    private String username;
    private String credential;
}

