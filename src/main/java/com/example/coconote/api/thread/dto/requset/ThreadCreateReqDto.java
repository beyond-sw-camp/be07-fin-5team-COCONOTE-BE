package com.example.coconote.api.thread.dto.requset;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.thread.entity.Thread;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadCreateReqDto {
    //TODO: jwt토큰이 완성되면 memberId 필요없어짐 삭제
    private Long memberId;
    private String content;
    private List<String> files;
    private Long parentId;
    private Long channelId;

    public Thread toEntity(Member member, Thread thread, Channel channel) {
        return Thread.builder()
                .member(member)
                .content(this.content)
                .files(this.getFiles())
                .parent(thread)
                .channel(channel)
                .build();
    }
}
