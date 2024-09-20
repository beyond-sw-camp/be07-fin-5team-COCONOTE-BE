package com.example.coconote.api.chatRoom.dto;

import com.example.coconote.api.chatRoom.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage2 {
    private MessageType type;
    private Long channelId;
    private Long sender;
    private String message;
}
