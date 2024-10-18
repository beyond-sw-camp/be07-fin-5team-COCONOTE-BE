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

    //    멀티 스레드 환경에서 동시성 문제를 해결하기 위해 ConcurrentHashMap과 ConcurrentLinkedQueue를 사용
    private final ConcurrentMap<Long, Map<Long, SseEmitter>> workspaceEmitters = new ConcurrentHashMap<>();
    private final ListOperations<String, Object> listOperations;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper 인스턴스

    private static final long SSE_TIMEOUT = 60 * 1000L; // 60초 타임아웃
    // 최소 5개의 스레드, 최대 50개의 스레드를 갖는 동적 스레드 풀 생성
    private static final ScheduledThreadPoolExecutor dynamicScheduler = new ScheduledThreadPoolExecutor(
            5, // corePoolSize: 기본적으로 유지할 스레드 수
            new ThreadPoolExecutor.CallerRunsPolicy() // 최대 스레드 수를 초과할 경우 실행할 정책
    );

    static {
        // 동적으로 최대 스레드 수를 설정
        dynamicScheduler.setMaximumPoolSize(50);
        // 일정 시간이 지나면 풀에서 비활성 스레드를 제거하도록 설정 (기본 60초)
        dynamicScheduler.setKeepAliveTime(60, TimeUnit.SECONDS);
        // 코어 스레드도 타임아웃 적용 (true로 설정 시 비활성 코어 스레드도 제거 가능)
        dynamicScheduler.allowCoreThreadTimeOut(true);
    }

    @Autowired
    public ThreadNotificationService(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
        this.listOperations = redisTemplate.opsForList();
    }

    public SseEmitter subscribe(Long userId, Long workspaceId) {
        // 이미 존재하는 Emitter가 있는지 확인
        Map<Long, SseEmitter> userEmitters = workspaceEmitters.computeIfAbsent(workspaceId, k -> new ConcurrentHashMap<>());
        SseEmitter existingEmitter = userEmitters.get(userId);
        if (existingEmitter != null) {
            // 이미 존재하는 Emitter를 반환
            log.info("이미 존재하는 Emitter가 있습니다. 사용자 ID: {}, 워크스페이스 ID: {}", userId, workspaceId);
            return existingEmitter;
        }

        // 새로운 Emitter 생성
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        userEmitters.put(userId, emitter);

        // 연결 상태와 관련된 핸들러 설정
        emitter.onCompletion(() -> removeEmitter(workspaceId, userId));
        emitter.onTimeout(() -> removeEmitter(workspaceId, userId));
        emitter.onError(e -> removeEmitter(workspaceId, userId));

        // 주기적으로 "ping" 메시지 전송 (스레드 풀 공유)
        dynamicScheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("keepAlive").data("ping"));
            } catch (IOException e) {
                removeEmitter(workspaceId, userId);
            }
        }, 0, 30, TimeUnit.SECONDS);

        // Emitter 개수 로그 출력
        logCurrentEmitterCount();

        return emitter;
    }

    public void sendNotification(Long senderId, Long workspaceId, Long channelId, String message, String channelName, String memberName) {
        // 알림 객체 생성
        NotificationDto notificationDto = new NotificationDto(senderId, workspaceId, channelId, message, channelName, memberName);

        try {
            // JSON 형식의 알림 메시지 생성
            String notificationMessage = objectMapper.writeValueAsString(notificationDto);

            // 해당 워크스페이스의 모든 구독자에게 알림 전송
            Map<Long, SseEmitter> emitters = workspaceEmitters.getOrDefault(workspaceId, new ConcurrentHashMap<>());
            for (Map.Entry<Long, SseEmitter> entry : emitters.entrySet()) {
                Long userId = entry.getKey();
                if (!userId.equals(senderId)) { // 발신자와 수신자가 같지 않은 경우에만 알림 저장 및 전송
                    // Redis에 사용자별로 알림 임시 저장 (채널 단위로 저장)
                    String userKey = getUserChannelKey(userId, channelId);
                    listOperations.rightPush(userKey, notificationMessage);

                    // 사용자별 읽지 않은 알림 개수 확인
                    Long notificationCount = listOperations.size(userKey);
                    log.info("User {} for channel {} has {} unread notifications", userId, channelId, notificationCount);

                    // 알림 전송
                    sendNotificationToEmitter(entry.getValue(), notificationMessage);
                }
            }
            // 발신자 정보가 포함된 알림 메시지를 Redis Pub/Sub을 통해 발송
            redisTemplate.convertAndSend(topic.getTopic(), notificationMessage);

            log.info("Notification sent to workspace {} for channel {}: {}", workspaceId, channelId, notificationMessage);
        } catch (JsonProcessingException e) {
            log.error("Notification JSON 변환 실패: ", e);
        }
    }


    public void markNotificationAsRead(Long userId, Long channelId) {
        // Redis에서 해당 사용자의 채널에 대한 읽지 않은 알림을 삭제
        String key = getUserChannelKey(userId, channelId);
        listOperations.getOperations().delete(key);
        log.info("Notifications marked as read for user {} and channel {}", userId, channelId);
    }

    private void sendNotificationToEmitter(SseEmitter emitter, String message) {
        CompletableFuture.runAsync(() -> {
            try {
                emitter.send(SseEmitter.event().name("notification").data(message));
                log.info("Notification sent to emitter: {}", message);
            } catch (IOException e) {
                log.error("알림 전송 중 오류 발생: ", e);
            }
        });
    }

    private void removeEmitter(Long workspaceId, Long userId) {
        Map<Long, SseEmitter> userEmitters = workspaceEmitters.get(workspaceId);
        if (userEmitters != null) {
            userEmitters.remove(userId);
            if (userEmitters.isEmpty()) {
                workspaceEmitters.remove(workspaceId);
            }
        }
        // Emitter 개수 로그 출력
        logCurrentEmitterCount();
    }

    private String getUserChannelKey(Long userId, Long channelId) {
        return "user:" + userId + ":channel:" + channelId;
    }

    public Long getUnreadNotificationCount(Long userId, Long channelId) {
        String key = getUserChannelKey(userId, channelId);
        Long count = listOperations.size(key);
        return count != null ? count : 0L;
    }

    private void logCurrentEmitterCount() {
        int totalEmitters = workspaceEmitters.values().stream()
                .mapToInt(Map::size)
                .sum();
        log.info("현재 존재하는 Emitter의 개수: {}", totalEmitters);
    }

}
