package com.example.coconote.api.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
public class ThreadNotificationService {

    private final ConcurrentMap<Long, Map<Long, SseEmitter>> workspaceEmitters = new ConcurrentHashMap<>();
    private final ListOperations<String, Object> listOperations;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long SSE_TIMEOUT = 60 * 1000L;
    private static final ScheduledThreadPoolExecutor dynamicScheduler = new ScheduledThreadPoolExecutor(
            5, new ThreadPoolExecutor.CallerRunsPolicy()
    );

    static {
        dynamicScheduler.setMaximumPoolSize(50);
        dynamicScheduler.setKeepAliveTime(60, TimeUnit.SECONDS);
        dynamicScheduler.allowCoreThreadTimeOut(true);
    }

    @Autowired
    public ThreadNotificationService(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
        this.listOperations = redisTemplate.opsForList();
    }

    public SseEmitter subscribe(Long userId, Long workspaceId) {
        Map<Long, SseEmitter> userEmitters = workspaceEmitters.computeIfAbsent(workspaceId, k -> new ConcurrentHashMap<>());
        SseEmitter existingEmitter = userEmitters.get(userId);
        if (existingEmitter != null) {
            existingEmitter.complete();
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        userEmitters.put(userId, emitter);
        setupEmitter(emitter, workspaceId, userId);
        scheduleKeepAlive(emitter);

        logEmitterCount();
        return emitter;
    }

    private void setupEmitter(SseEmitter emitter, Long workspaceId, Long userId) {
        emitter.onCompletion(() -> removeEmitter(workspaceId, userId));
        emitter.onTimeout(() -> removeEmitter(workspaceId, userId));
        emitter.onError(e -> removeEmitter(workspaceId, userId));
    }

    private void scheduleKeepAlive(SseEmitter emitter) {
        dynamicScheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("keepAlive").data("ping"));
            } catch (IOException e) {
                emitter.complete();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void sendNotification(Long senderId, Long workspaceId, Long channelId, String message, String channelName, String memberName) {
        NotificationDto notification = new NotificationDto(senderId, workspaceId, channelId, message, channelName, memberName);
        try {
            String notificationMessage = objectMapper.writeValueAsString(notification);
            broadcastNotification(workspaceId, senderId, channelId, notificationMessage);
            redisTemplate.convertAndSend(topic.getTopic(), notificationMessage);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert notification to JSON", e);
        }
    }

    private void broadcastNotification(Long workspaceId, Long senderId, Long channelId, String notificationMessage) {
        Map<Long, SseEmitter> emitters = workspaceEmitters.getOrDefault(workspaceId, new ConcurrentHashMap<>());
        for (Map.Entry<Long, SseEmitter> entry : emitters.entrySet()) {
            if (!entry.getKey().equals(senderId)) {
                String userKey = getUserChannelKey(entry.getKey(), channelId);
                listOperations.rightPush(userKey, notificationMessage);
                sendNotificationToEmitter(entry.getValue(), notificationMessage);
            }
        }
    }

    private void sendNotificationToEmitter(SseEmitter emitter, String message) {
        CompletableFuture.runAsync(() -> {
            try {
                emitter.send(SseEmitter.event().name("notification").data(message));
            } catch (IOException e) {
                log.error("Failed to send notification", e);
            }
        });
    }

    public void markNotificationAsRead(Long userId, Long channelId) {
        String key = getUserChannelKey(userId, channelId);
        listOperations.getOperations().delete(key);
        log.info("Marked notifications as read for user {} and channel {}", userId, channelId);
    }

    public Long getUnreadNotificationCount(Long userId, Long channelId) {
        String key = getUserChannelKey(userId, channelId);
        Long count = listOperations.size(key);
        return count != null ? count : 0L;
    }

    private void removeEmitter(Long workspaceId, Long userId) {
        Map<Long, SseEmitter> userEmitters = workspaceEmitters.get(workspaceId);
        if (userEmitters != null) {
            userEmitters.remove(userId);
            if (userEmitters.isEmpty()) {
                workspaceEmitters.remove(workspaceId);
            }
        }
        logEmitterCount();
    }

    private void logEmitterCount() {
        int totalEmitters = workspaceEmitters.values().stream().mapToInt(Map::size).sum();
        log.info("Total active emitters: {}", totalEmitters);
    }

    private String getUserChannelKey(Long userId, Long channelId) {
        return "user:" + userId + ":channel:" + channelId;
    }
}
