package com.example.coconote.api.canvas.canvas.controller;

import com.example.coconote.api.canvas.canvas.dto.request.ChatMessage;
import com.example.coconote.api.canvas.canvas.dto.response.CanvasListResDto;
import com.example.coconote.api.canvas.canvas.service.CanvasService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Controller
public class WebsocketController {


    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CanvasService canvasService;

    /**
     * ⭐ 캔버스용
     * websocket "/pub/canvas/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/canvas/message")
    public void message(ChatMessage message) {
        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            canvasService.enterChatRoom(message.getRoomId());
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }

        System.out.println(canvasService.getTopic(message.getRoomId()));

        kafkaTemplate.send("canvas-topic", message);
    }

    // 모든 채팅방 목록 반환
    @GetMapping("/canvas/rooms")
    @ResponseBody
    public List<CanvasListResDto> room() {
        return canvasService.findAllRoom();
    }

}