package com.example.coconote.api.chat.dto;

import com.example.coconote.api.chat.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private MessageType type;
    private String roomId;
    private String sender;
    private String message;
}
