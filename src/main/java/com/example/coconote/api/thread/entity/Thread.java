package com.example.coconote.api.thread.entity;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.tag.dto.response.TagResDto;
import com.example.coconote.api.thread.dto.requset.ThreadReqDto;
import com.example.coconote.api.thread.dto.response.ThreadResDto;
import com.example.coconote.api.threadTag.entity.ThreadTag;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Thread extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="thread_id")
    private Long id;
    private String content;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> files;
    @ManyToOne(fetch = FetchType.LAZY)
    private Thread parent;
    //TODO:추후 워크스페이스-유저로 변경
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    private Channel channel;
    @Builder.Default
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL)
    private List<ThreadTag> threadTags = new ArrayList<>();

    public ThreadResDto fromEntity() {
        List<TagResDto> tags = this.threadTags.stream().map(threadTag -> threadTag.fromEntity()).toList();
        return ThreadResDto.builder()
                .id(this.id)
                .memberName(this.member.getNickname())
                .createdTime(this.getCreatedTime().toString())
                .content(this.content)
                .files(this.files)
                .tags(tags)
                .build();
    }

    public ThreadResDto fromEntity(MessageType type) {
        List<TagResDto> tags = this.threadTags.stream().map(threadTag -> threadTag.fromEntity()).toList();
        return ThreadResDto.builder()
                .id(this.id)
                .type(type)
                .memberName(this.member.getNickname())
                .createdTime(this.getCreatedTime().toString())
                .content(this.content)
                .files(this.files)
                .tags(tags)
                .build();
    }

    public ThreadResDto fromEntity(List<ThreadResDto> childThreadList) {
        List<TagResDto> tags = this.threadTags.stream().map(threadTag -> threadTag.fromEntity()).toList();
        return ThreadResDto.builder()
                .id(this.id)
                .memberName(this.member.getNickname())
                .createdTime(this.getCreatedTime().toString())
                .content(this.content)
                .files(this.files)
                .childThreads(childThreadList)
                .tags(tags)
                .build();
    }

    // 소프트 삭제 메서드
    public void markAsDeleted() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
    }

    public void updateThread(ThreadReqDto threadReqDto) {
        this.content = threadReqDto.getContent();
        this.files = threadReqDto.getFiles();
    }
}
