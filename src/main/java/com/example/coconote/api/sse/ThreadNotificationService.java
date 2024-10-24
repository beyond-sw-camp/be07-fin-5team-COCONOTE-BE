package com.example.coconote.api.sse;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channelMember.repository.ChannelMemberRepository;
import com.example.coconote.api.search.mapper.WorkspaceMemberMapper;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThreadNotificationService {

    // Emitters getter 메서드
    @Getter
    private final Map<Long, Map<Long, SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChannelMemberRepository channelMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    // 사용자별 워크스페이스 알림 구독
    public SseEmitter subscribe(Long memberId, Long workspaceId) {
        // 타임아웃을 30분으로 설정
        SseEmitter emitter = new SseEmitter( 30 * 60 * 1000L); 

        // 워크스페이스 단위로 SseEmitter 저장
        Map<Long, SseEmitter> workspaceEmitters = emitters.computeIfAbsent(workspaceId, w -> new ConcurrentHashMap<>());

        // 이미 존재하는 Emitter가 있는지 확인
        if (workspaceEmitters.containsKey(memberId)) {
            log.warn("Emitter already exists for memberId={}, workspaceId={}", memberId, workspaceId);
//            emitter 갱신
            removeEmitter(workspaceId, memberId);
        }

        workspaceEmitters.put(memberId, emitter);

        emitter.onCompletion(() -> removeEmitter(workspaceId, memberId));
        emitter.onTimeout(() -> removeEmitter(workspaceId, memberId));
        emitter.onError((e) -> {
            log.error("SSE Emitter error for memberId={}, workspaceId={}", memberId, workspaceId, e);
            removeEmitter(workspaceId, memberId);
        });
        // Emitter 개수 로그 출력 메서드 호출
        logEmitterCount(workspaceId, workspaceEmitters.size());

        log.info("SSE emitter created for memberId={}, workspaceId={}", memberId, workspaceId);
// 구독 시 초기 메시지 전송
        sendWelcomeNotification(emitter, memberId, workspaceId);
        return emitter;
    }

    // 구독 시 사용자에게 환영 메시지를 Emitter로 전송하는 메서드
    private void sendWelcomeNotification(SseEmitter emitter, Long memberId, Long workspaceId) {
        String message = "Welcome to workspace " + workspaceId + "!"; // 환영 메시지

        // 알림 데이터를 구성합니다.
        NotificationDto notification = NotificationDto.builder()
                .userId(memberId)
                .workspaceId(workspaceId)
                .channelId(null) // 예시로 null, 필요시 적절한 값으로 변경
                .channelName(null) // 예시로 null, 필요시 적절한 값으로 변경
                .threadId(null) // 예시로 null, 필요시 적절한 값으로 변경
                .parentThreadId(null) // 예시로 null, 필요시 적절한 값으로 변경
                .message(message)
                .memberName("System") // 알림을 보낸 주체
                .build();

        // Emitter를 통해 메시지를 직접 전송합니다.
        try {
            emitter.send(SseEmitter.event()
                    .name("notification") // 이벤트 이름
                    .data(objectMapper.writeValueAsString(notification))); // JSON 문자열로 전송
        } catch (IOException e) {
            log.error("Failed to send welcome notification to memberId={}, workspaceId={}", memberId, workspaceId, e);
            removeEmitter(workspaceId, memberId); // Emitter 제거
        }
    }

    // Emitter 제거 메서드
    public void removeEmitter(Long workspaceId, Long memberId) {
        Map<Long, SseEmitter> workspaceEmitters = emitters.get(workspaceId);
        if (workspaceEmitters != null) {
            workspaceEmitters.remove(memberId);
            if (workspaceEmitters.isEmpty()) {
                emitters.remove(workspaceId);
            }
            log.info("Emitter removed for memberId={}, workspaceId={}", memberId, workspaceId);
            // Emitter 개수 로그 출력 메서드 호출
            logEmitterCount(workspaceId, workspaceEmitters.size());
        }
    }


    // 알림 전송: Redis 채널을 통해 전파
    public void sendNotification(WorkspaceMember member, Workspace workspace, Channel channel,
                                 Thread thread, Thread parentThread) {
        NotificationDto notification = NotificationDto.builder()
                .userId(member.getWorkspaceMemberId())
                .workspaceId(workspace.getWorkspaceId())
                .channelId(channel.getChannelId())
                .channelName(channel.getChannelName())
                .threadId(thread.getId())
                .parentThreadId(parentThread != null ? parentThread.getId() : null)
                .message(thread.getContent())
                .memberName(member.getNickname())
                .build();

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("workspaceId", workspace.getWorkspaceId());
        messageMap.put("channelId", channel.getChannelId());
        messageMap.put("notification", notification);

        try {
            String messageJson = objectMapper.writeValueAsString(messageMap);
            log.info("Sending redisMessage: {}", messageJson);
            redisTemplate.convertAndSend("notification-channel", messageJson);
            log.info("Notification sent successfully: {}", notification);

//            redis에 읽지 않은 알림 수 증가
            incrementUnreadCount(member.getWorkspaceMemberId(), channel.getChannelId());
        } catch (JsonProcessingException e) {
            log.error("Failed to convert notification to JSON", e);
        }
    }

    // 주기적으로 비활성화된 Emitter를 정리하는 메서드
    public void cleanUpEmitters() {
        emitters.forEach((workspaceId, workspaceEmitters) ->
                workspaceEmitters.entrySet().removeIf(entry -> {
                    SseEmitter emitter = entry.getValue();
                    boolean isCompleted = emitter == null || emitter.toString().contains("completed");
                    if (isCompleted) {
                        log.info("Removing completed Emitter for memberId={}, workspaceId={}", entry.getKey(), workspaceId);
                        return true; // Emitter를 제거하도록 반환
                    }
                    return false;
                })
        );
    }

    public void sendNotificationToWorkspaceMembers(Long workspaceId, String notificationJson) {
        Map<Long, Map<Long, SseEmitter>> emitters = getEmitters();

        if (emitters.containsKey(workspaceId)) {
            emitters.get(workspaceId).forEach((memberId, emitter) -> {
                // 받는 사용자가 해당 채널에 가입되었는지 확인
                Long channelId = extractChannelIdFromNotification(notificationJson);
//                워크스페이스 아이디와 멤버 아이디로 워크스페이스 멤버를 찾는다.
                WorkspaceMember workspaceMember = workspaceMemberRepository.findByWorkspace_WorkspaceIdAndMember_Id(workspaceId, memberId);

                if (!isUserSubscribedToChannel(workspaceMember.getWorkspaceMemberId(), channelId)) {
                    log.info("User {} is not subscribed to channel {}. Skipping notification.", memberId, channelId);
                    return; // 채널에 가입되지 않은 경우 알림 전송 중단
                }

                try {
                    emitter.send(SseEmitter.event()
                            .name("notification")
                            .data(notificationJson));
                } catch (IOException e) {
                    emitter.complete();
                    removeEmitter(workspaceId, memberId);
                    log.error("Failed to send notification to memberId={}, workspaceId={}", memberId, workspaceId);
                }
            });
        } else {
            log.warn("No subscribers found for workspaceId={}", workspaceId);
        }
    }


    // 알림 JSON에서 채널 ID를 추출하는 메서드
    private Long extractChannelIdFromNotification(String notificationJson) {
        try {
            Map<String, Object> notificationMap = objectMapper.readValue(notificationJson, Map.class);
            return Long.valueOf(notificationMap.get("channelId").toString());
        } catch (Exception e) {
            log.error("Failed to extract channelId from notification JSON", e);
            return null;
        }
    }

    // 사용자가 해당 채널에 가입되어 있는지 확인하는 메서드
    private boolean isUserSubscribedToChannel(Long userId, Long channelId) {
        return channelMemberRepository.existsByWorkspaceMember_WorkspaceMemberIdAndChannel_ChannelId(userId, channelId);
    }

    // Redis에 읽지 않은 알림 수 증가
    private void incrementUnreadCount(Long userId, Long channelId) {
        redisTemplate.opsForValue().increment("unread_notifications:" + userId + ":" + channelId);
    }

    // 사용자와 채널에 대한 읽지 않은 알림 수를 Redis에서 가져오는 메서드
    public Long getUnreadCount(Long userId, Long channelId) {
        String key = "unread_notifications:" + userId + ":" + channelId;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.valueOf(count) : 0L;
    }

    // 사용자와 채널에 대한 읽지 않은 알림 수를 Redis에서 삭제하는 메서드
    public void markAsRead(Long userId, Long channelId) {
        String key = "unread_notifications:" + userId + ":" + channelId;
        redisTemplate.delete(key); // 읽음 처리
    }

    // Emitter 개수 로그 출력 메서드
    private void logEmitterCount(Long workspaceId, int count) {
        log.info("Total emitters for workspaceId {}: {}", workspaceId, count);
    }
}
