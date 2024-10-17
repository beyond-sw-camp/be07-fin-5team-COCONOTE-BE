// NotificationMessageListener.java
package com.example.coconote.api.sse;

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

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String notificationMessage = new String(message.getBody());
        log.info("Received notification message from Redis: {}", notificationMessage);
        // 추가적인 처리를 할 수 있습니다.
    }
}
