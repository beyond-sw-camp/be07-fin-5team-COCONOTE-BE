package com.example.coconote.chatTest;

import com.example.coconote.chatTest.dto.ChatMessage;
import com.example.coconote.chatTest.dto.ChatRoom;
import com.example.coconote.chatTest.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;



@RequiredArgsConstructor
@Controller
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            chatRoomRepository.enterChatRoom(message.getRoomId());
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }

        System.out.println(chatRoomRepository.getTopic(message.getRoomId()));
//        kafka 영역
//        kafkaTemplate.send("product-update-topic",
//                String.valueOf(chatRoomRepository.getTopic(message.getRoomId())), message);

        kafkaTemplate.send("product-update-topic", message);


        // Websocket에 발행된 메시지를 redis로 발행한다(publish)
//        redisPublisher.publish(chatRoomRepository.getTopic(message.getRoomId()), message);
    }
}