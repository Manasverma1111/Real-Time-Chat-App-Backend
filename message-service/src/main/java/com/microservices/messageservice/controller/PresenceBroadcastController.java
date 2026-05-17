package com.microservices.messageservice.controller;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal")
public class PresenceBroadcastController {

    private final SimpMessagingTemplate messagingTemplate;

//    presence-service will call this endpoint to broadcast presence updates to all connected frontends via WebSocket.
    public PresenceBroadcastController(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    /*
     Called by presence-service when a user goes ONLINE or OFFLINE.
     Broadcasts to /topic/presence so all connected frontends
     update their online counts in real time.
    */
    @PostMapping("/presence")
    public void broadcastPresence(
            @RequestBody Map<String, String> payload
    ) {
        messagingTemplate.convertAndSend(
                "/topic/presence",
                payload
        );

        System.out.println(
                " Presence broadcast: "
                        + payload.get("userId")
                        + " → "
                        + payload.get("status")
        );
    }
}