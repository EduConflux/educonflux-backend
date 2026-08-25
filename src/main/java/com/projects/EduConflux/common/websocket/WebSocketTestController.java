package com.projects.EduConflux.common.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketTestController {

    @MessageMapping("/test")
    @SendTo("/topic/test")
    public String testMessage(String message) {

        return message;
    }
}