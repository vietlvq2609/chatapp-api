package com.vietle.mychatapi.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketSubscriptionHandler {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void subscribeToConversation(Long conversationId, String username) {
        String topic = "/topic/conversation/" + conversationId;
        String message = username + " joined to conversation " + conversationId;
        messagingTemplate.convertAndSend(topic, message);
    }
}
