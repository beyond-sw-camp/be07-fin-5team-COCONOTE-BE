package com.example.coconote.api.thread.entity;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.tag.dto.response.TagResDto;
import com.example.coconote.api.thread.dto.response.ThreadResDto;
import com.example.coconote.api.threadTag.entity.ThreadTag;
import com.example.coconote.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
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
                .createdTime(this.getCreatedTime())
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
                .createdTime(this.getCreatedTime())
                .content(this.content)
                .files(this.files)
                .childThreads(childThreadList)
                .tags(tags)
                .build();
    }
}
