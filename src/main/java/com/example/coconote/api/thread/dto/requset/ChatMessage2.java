package com.example.coconote.api.thread.dto.requset;

import com.example.coconote.api.thread.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage2 {
    private MessageType type;
    private Long channelId;
    private Long sender;
    private Long parentThreadId;
    private String message;
    private List<String> files;
}
