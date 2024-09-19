package com.example.coconote.api.chat.entity;

import com.example.coconote.api.chat.dto.ChatMessage;
import com.example.coconote.api.chat.service.ChatService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {
    private String roomId;
    private String name;

    public static ChatRoom create(String name){
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = UUID.randomUUID().toString();
        chatRoom.name = name;
        return chatRoom;
    }



//    private Set<WebSocketSession> sessions = new HashSet<>();
//
//    public void handleActions(WebSocketSession session, ChatMessage chatMessage, ChatService chatService){
//        if(chatMessage.getType().equals(MessageType.ENTER)){
//            sessions.add(session);
//            chatMessage.setMessage(chatMessage.getSender() + "님이 입장했습니다.");
//        }
//        sendMessage(chatMessage, chatService);
//    }
//
//    public <T> void sendMessage(T message, ChatService chatService){
//        sessions.parallelStream().forEach(session -> chatService.sendMessage(session, message));
//    }
}
