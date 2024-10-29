package com.example.coconote.api.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageListener implements MessageListener {

    private final ThreadNotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String notificationMessage = new String(message.getBody());
        log.info("Received notification message from Redis: {}", notificationMessage);

        try {
            NotificationMessage messageObj = objectMapper.readValue(notificationMessage, NotificationMessage.class);
            Long workspaceId = messageObj.getWorkspaceId();
            String notificationJson = objectMapper.writeValueAsString(messageObj.getNotification());

            notificationService.sendNotificationToWorkspaceMembers(workspaceId, notificationJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse notification message: {}", notificationMessage, e);
        }
    }
}
