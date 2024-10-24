package com.example.coconote.api.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 알림 객체 클래스
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
        private Long userId;
        private Long workspaceId;
        private Long channelId;
        private String channelName;
        private Long threadId;
        private Long parentThreadId;
        private String message;
        private String memberName;
}
