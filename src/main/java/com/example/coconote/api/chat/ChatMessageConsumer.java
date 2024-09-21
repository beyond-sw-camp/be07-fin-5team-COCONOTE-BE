package com.example.coconote.api.chat;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatMessageConsumer {
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "chat_topic", groupId = "my-consumer-group")
    public void listen(ConsumerRecord<String, ThreadRequestDto> record) {
        ThreadRequestDto threadRequestDto = record.value();

        // 수신한 메시지를 채널로 브로드캐스트하기 전에 로그 찍기
        log.info("Received message from Kafka: {}", threadRequestDto);
        // 수신한 메시지를 채널로 브로드캐스트
        messagingTemplate.convertAndSend("/topic/" + threadRequestDto.getChannelId(), threadRequestDto);
    }
}