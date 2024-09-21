package com.example.coconote.api.chat;

import com.example.coconote.api.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.entity.Thread;
import com.example.coconote.api.thread.service.ThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ThreadChatController {
    private final ThreadService threadService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/sendThread") // 클라이언트가 이 엔드포인트로 메시지를 보냄
//    @SendTo("/topic/{channelId}") // 해당 채널에 브로드캐스트
//    멤버 아이디, 채널 아이디, 부모 아이디, 내용, 파일
    public ThreadResDto createThread(ThreadRequestDto request) {
        Thread thread = threadService.createChatThread(request);

        // 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/topic/" + request.getChannelId(), thread.toDto());

        return thread.toDto();
    }

    @MessageMapping("/getThreads/{channelId}")
    public List<ThreadResDto> getThreads(Long channelId) {
        return threadService.getThreads(channelId);
    }

}
