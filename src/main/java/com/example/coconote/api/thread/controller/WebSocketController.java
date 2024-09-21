package com.example.coconote.api.thread.controller;

// import 생략...

import com.example.coconote.api.thread.dto.requset.ChatMessage2;
import com.example.coconote.api.chatRoom.entity.MessageType;
import com.example.coconote.api.thread.dto.requset.ThreadCreateReqDto;
import com.example.coconote.api.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.service.ThreadService;
import com.example.coconote.api.thread.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class WebSocketController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ThreadService threadService;

    @MessageMapping("/chat/message")
    public void message(ThreadCreateReqDto message) {
        if (MessageType.ENTER.equals(message.getType()))
            message.setContent(message.getSenderId() + "님이 입장하셨습니다.");

        ThreadResDto threadResDto = threadService.createThread(message);
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getChannelId(), threadResDto);
    }
}