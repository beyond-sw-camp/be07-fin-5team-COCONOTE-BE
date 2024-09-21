package com.example.coconote.api.chat;

import com.example.coconote.api.thread.entity.Thread;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadRequestDto {
    private String content;
    private List<String> files;
    private Long memberId;
    private Long channelId;
    private Long parentId; // 응답 메시지의 경우 부모 ID

}