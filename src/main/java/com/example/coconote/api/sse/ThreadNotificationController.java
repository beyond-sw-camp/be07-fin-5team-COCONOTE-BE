package com.example.coconote.api.sse;

import com.example.coconote.security.token.JwtTokenProvider;
import com.example.coconote.security.util.CustomPrincipal;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class ThreadNotificationController {

    private final ThreadNotificationService threadNotificationService;
    private final JwtTokenProvider jwtTokenProvider;


    // SSE 구독 엔드포인트: 사용자가 워크스페이스의 알림을 구독할 수 있게 합니다.
    @CrossOrigin(origins = "http://localhost:8082", allowCredentials = "true")
    @GetMapping(value = "/subscribe/{workspaceId}", produces = "text/event-stream")
    public SseEmitter subscribe(@PathVariable Long workspaceId, @RequestParam("token") String token) {

        // JWT 토큰을 검증하고 사용자 정보를 추출
        jwtTokenProvider.validateToken(token);
        Long memberId = jwtTokenProvider.getMemberIdFromToken(token);
        log.info("Subscribe to notifications for member {} and workspace {}", memberId, workspaceId);
        return threadNotificationService.subscribe(memberId, workspaceId);
    }

    // 사용자별 채널의 읽지 않은 알림 수를 반환하는 엔드포인트
    @GetMapping("/unread/count/{channelId}")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long channelId ,@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Long memberId = customPrincipal.getMemberId();

        // Redis에서 읽지 않은 알림 수 조회
        Long unreadCount = threadNotificationService.getUnreadCount(memberId, channelId);
        return ResponseEntity.ok(unreadCount);
    }

    // 사용자별 채널 알림 삭제 엔드포인트: 사용자가 특정 채널의 알림을 확인했을 때 호출
    @DeleteMapping("/mark-as-read/{channelId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long channelId, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Long memberId = customPrincipal.getMemberId();

        // Redis에서 해당 채널의 읽지 않은 알림 수를 삭제
        threadNotificationService.markAsRead(memberId, channelId);
        return ResponseEntity.noContent().build();
    }

}

