package com.example.coconote.api.thread.service;

import com.example.coconote.api.thread.dto.requset.ChatMessage2;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessageSendingOperations messagingTemplate;

    public void sendMessage(ChatMessage2 message) {

        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getChannelId(), message);
    }
}
