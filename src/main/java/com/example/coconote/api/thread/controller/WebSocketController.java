package com.example.coconote.api.thread.controller;

import com.example.coconote.api.thread.entity.MessageType;
import com.example.coconote.api.thread.dto.requset.ThreadCreateReqDto;
import com.example.coconote.api.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.service.ThreadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class WebSocketController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ThreadService threadService;
    private final KafkaTemplate<String, ThreadCreateReqDto> kafkaThreadTemplate;

    @MessageMapping("/chat/message")
    public void message(ThreadCreateReqDto message) {
        if (MessageType.ENTER.equals(message.getType()))
            message.setContent(message.getSenderId() + "님이 입장하셨습니다.");

//        ThreadResDto threadResDto = threadService.createThread(message);

        kafkaThreadTemplate.send("chat_topic", message.getChannelId().toString(), message);
//        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getChannelId(), threadResDto);
    }

    @KafkaListener(topics = "chat_topic", groupId = "my-consumer-group")
    public void listen(ConsumerRecord<String, ThreadCreateReqDto> record) {
        ThreadCreateReqDto threadCreateReqDto = record.value();

        ThreadResDto threadResDto = threadService.createThread(threadCreateReqDto);
        // 수신한 메시지를 채널로 브로드캐스트하기 전에 로그 찍기
        log.info("Received message from Kafka: {}", threadResDto);
        // 수신한 메시지를 채널로 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chat/room/" + threadCreateReqDto.getChannelId(), threadResDto);
    }
}