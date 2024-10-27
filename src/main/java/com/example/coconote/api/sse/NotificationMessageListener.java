package com.example.coconote.api.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageListener implements MessageListener {

    private final ThreadNotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String notificationMessage = new String(message.getBody());
        log.info("Received notification message from Redis: {}", notificationMessage);

        try {
            // JSON 형식으로 역직렬화하여 메시지 파싱
            Map<String, Object> messageMap = objectMapper.readValue(notificationMessage, Map.class);
            Long workspaceId = Long.valueOf(messageMap.get("workspaceId").toString());
            String notificationJson = objectMapper.writeValueAsString(messageMap.get("notification"));

            // 서비스 메서드를 통해 알림 전송
            notificationService.sendNotificationToWorkspaceMembers(workspaceId, notificationJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse notification message: {}", notificationMessage, e);
        }
    }

}
