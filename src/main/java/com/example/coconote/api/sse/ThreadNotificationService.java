package com.example.coconote.api.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class ThreadNotificationService {

    private final ConcurrentMap<Long, List<SseEmitter>> workspaceEmitters = new ConcurrentHashMap<>();
    private final ListOperations<String, Object> listOperations;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;

    @Autowired
    public ThreadNotificationService(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
        this.listOperations = redisTemplate.opsForList();
    }

    public SseEmitter subscribe(Long userId, Long workspaceId) {
        SseEmitter emitter = new SseEmitter(60 * 1000L);

        // 워크스페이스 ID를 키로 사용해 워크스페이스에 구독자 추가
        workspaceEmitters.computeIfAbsent(workspaceId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(workspaceId, emitter));
        emitter.onTimeout(() -> removeEmitter(workspaceId, emitter));
        emitter.onError(e -> removeEmitter(workspaceId, emitter));

        return emitter;
    }

    @Async
    public void sendNotification(Long userId, Long workspaceId, Long channelId, String message, String channelName, String memberName) {
        // JSON 형식의 알림 메시지 생성
        String notificationMessage = String.format(
                "{\"userId\": %d, \"workspaceId\": %d, \"channelId\": %d, \"channelName\": \"%s\", \"memberName\": \"%s\", \"message\": \"%s\"}",
                userId, workspaceId, channelId, channelName, memberName, message
        );
        // Redis에 사용자별로 알림 임시 저장 (채널 단위로 저장)
        String userKey = getUserChannelKey(userId, channelId);
        listOperations.rightPush(userKey, notificationMessage);

        Long notificationCount = listOperations.size(userKey);
        log.info("User {} for channel {} has {} unread notifications", userId, channelId, notificationCount);

        // Redis Pub/Sub을 통해 알림 발송
        redisTemplate.convertAndSend(topic.getTopic(), notificationMessage);

        // 해당 워크스페이스의 모든 구독자에게 알림 전송
        List<SseEmitter> emitters = workspaceEmitters.get(workspaceId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                sendNotificationToEmitter(emitter, notificationMessage);
            }
        }
        log.info("Notification sent to workspace {} for channel {}: {}", workspaceId, channelId, notificationMessage);
    }

    public void markNotificationAsRead(Long userId, Long channelId) {
        // Redis에서 해당 사용자의 채널에 대한 읽지 않은 알림을 삭제
        String key = getUserChannelKey(userId, channelId);
        listOperations.getOperations().delete(key);
        log.info("Notifications marked as read for user {} and channel {}", userId, channelId);
    }

    private void sendNotificationToEmitter(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("notification").data(message));
            log.info("Notification sent to emitter: {}", message);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private void removeEmitter(Long workspaceId, SseEmitter emitter) {
        List<SseEmitter> emitters = workspaceEmitters.get(workspaceId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                workspaceEmitters.remove(workspaceId);
            }
        }
    }

    private String getUserChannelKey(Long userId, Long channelId) {
        return "user:" + userId + ":channel:" + channelId;
    }

    public Long getUnreadNotificationCount(Long userId, Long channelId) {
        String key = getUserChannelKey(userId, channelId);
        Long count = listOperations.size(key);
        return count != null ? count : 0L;
    }
}

