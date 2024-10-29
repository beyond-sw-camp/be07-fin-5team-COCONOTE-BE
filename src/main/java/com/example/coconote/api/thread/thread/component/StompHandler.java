package com.example.coconote.api.thread.thread.component;

import com.example.coconote.security.token.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final AtomicInteger sessionCount = new AtomicInteger(0); // 세션 수를 관리하는 변수

    //    websocket을 통해 들어온 요청이 처리되기전 실행된다
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();


        if (StompCommand.CONNECT == accessor.getCommand()) {
            jwtTokenProvider.validateToken(accessor.getFirstNativeHeader("Authorization"));
            int currentCount = sessionCount.incrementAndGet(); // 세션 수 증가
            log.info("New connection established. Current session count: {}", currentCount);
            log.info("연결후 Session ID: {}", sessionId);
        } else if (StompCommand.DISCONNECT == accessor.getCommand()) {
            if (sessionCount.get() > 0) { // 세션 수가 0보다 클 때만 감소
                int currentCount = sessionCount.decrementAndGet(); // 세션 수 감소
                log.info("Connection closed. Current session count: {}", currentCount);
                log.info("해제후 Session ID: {}", sessionId);
            } else {
                log.warn("이미 세션이 0인데 끊김...");
            }
        }

        return message;
    }
    public int getSessionCount() {
        return sessionCount.get();
    }

}
