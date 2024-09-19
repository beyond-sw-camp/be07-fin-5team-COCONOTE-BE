package com.example.coconote.chatTest;

import com.example.coconote.chatTest.dto.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final SimpMessageSendingOperations messagingTemplate;

    public ChatService(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "product-update-topic", groupId = "order-group"
            , containerFactory = "kafkaListenerContainerFactory")
    public void consumerProductQuantity(String message){ // return 시, string 형식으로 message가 들어옴
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println(message);
            // ChatMessage 객채로 맵핑
            ChatMessage roomMessage =  objectMapper.readValue(message,ChatMessage.class);
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), roomMessage);

//            ProductUpdateStockDto productUpdateStockDto =
//                    objectMapper.readValue(message,ProductUpdateStockDto.class);
//            this.productStockUpdate(productUpdateStockDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e){
//            만약, 실패했을 때 코드 추가해야함
        }
        System.out.println(message);
    }
}
