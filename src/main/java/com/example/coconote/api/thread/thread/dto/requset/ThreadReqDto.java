package com.example.coconote.api.thread.thread.dto.requset;

import com.example.coconote.api.thread.thread.entity.MessageType;
import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.thread.thread.entity.Thread;
import com.example.coconote.api.thread.threadFile.dto.request.ThreadFileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadReqDto {
    //TODO: jwt토큰이 완성되면 memberId 필요없어짐 삭제
    private MessageType type;
    private Long senderId;
    private String content;
    private List<ThreadFileDto> files;
    private Long threadId;
    private Long parentId;
    private Long channelId;

    public Thread toEntity(Member member, Thread thread, Channel channel) {
        return Thread.builder()
                .member(member)
                .content(this.content)
                .parent(thread)
                .channel(channel)
                .build();
    }
}
