package com.example.coconote.api.thread.thread.entity;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.thread.tag.dto.response.TagResDto;
import com.example.coconote.api.thread.thread.dto.requset.ThreadReqDto;
import com.example.coconote.api.thread.thread.dto.response.ThreadResDto;
import com.example.coconote.api.thread.threadFile.dto.request.ThreadFileDto;
import com.example.coconote.api.thread.threadFile.entity.ThreadFile;
import com.example.coconote.api.thread.threadTag.entity.ThreadTag;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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

    @Column(length = 3000)
    private String content;
//    @ElementCollection(fetch = FetchType.EAGER)
//    private List<String> files;
    @ManyToOne(fetch = FetchType.LAZY)
    private Thread parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Thread> childThreads = new ArrayList<>(); // 자식 쓰레드 목록
    //TODO:추후 워크스페이스-유저로 변경
//    @ManyToOne(fetch = FetchType.LAZY)
//    private Member member;

    private Long canvasId;
    private String canvasTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_member_id")
    private WorkspaceMember workspaceMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Builder.Default
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ThreadTag> threadTags = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ThreadFile> threadFiles = new ArrayList<>();

    public ThreadResDto fromEntity() {
        List<TagResDto> tags = this.threadTags.stream()
                .filter(threadTag -> threadTag.getTag().getIsDeleted().equals(IsDeleted.N))
                .map(ThreadTag::fromEntity)
                .toList();
        List<ThreadFileDto> files = this.threadFiles.stream()
                .map(ThreadFile::fromEntity)
                .toList();
        return ThreadResDto.builder()
                .id(this.id)
                .image(this.workspaceMember.getProfileImage())
                .memberName(this.workspaceMember.getNickname())
                .memberId(this.workspaceMember.getWorkspaceMemberId())
                .createdTime(this.getCreatedTime().toString())
                .content(this.content)
                .files(files) // 파일 정보를 포함
                .tags(tags)
                .parentThreadId(this.parent != null ? this.parent.getId() : null)
                .canvasId(this.canvasId)
                .canvasTitle(this.canvasTitle)
                .build();
    }

    public ThreadResDto fromEntity(List<ThreadFileDto> files) {
        return ThreadResDto.builder()
                .id(this.id)
                .image(this.workspaceMember.getProfileImage())
                .memberName(this.workspaceMember.getNickname())
                .memberId(this.workspaceMember.getWorkspaceMemberId())
                .createdTime(this.getCreatedTime().toString())
                .content(this.content)
                .files(files)
                .tags(new ArrayList<>())
                .parentThreadId(this.parent != null ? this.parent.getId() : null)
                .canvasId(this.canvasId)
                .canvasTitle(this.canvasTitle)
                .build();
    }

    public ThreadResDto fromEntity(MessageType type) {
        List<TagResDto> tags = this.threadTags.stream().filter(threadTag -> threadTag.getTag().getIsDeleted().equals(IsDeleted.N)).map(threadTag -> threadTag.fromEntity()).toList();
        List<ThreadFileDto> files = this.threadFiles.stream().map(ThreadFile::fromEntity).toList();
        return ThreadResDto.builder()
                .id(this.id)
                .type(type)
                .image(this.workspaceMember.getProfileImage())
                .memberName(this.workspaceMember.getNickname())
                .memberId(this.workspaceMember.getWorkspaceMemberId())
                .createdTime(this.getCreatedTime().toString())
                .content(this.content)
                .files(files)
                .tags(tags)
                .parentThreadId(this.parent != null ? this.parent.getId() : null)
                .canvasId(this.canvasId)
                .canvasTitle(this.canvasTitle)
                .build();
    }

    public ThreadResDto fromEntity(List<ThreadResDto> childThreadList, List<ThreadFileDto> fileDtos) {
        List<TagResDto> tags = this.threadTags.stream().filter(threadTag -> threadTag.getTag().getIsDeleted().equals(IsDeleted.N)).map(ThreadTag::fromEntity).toList();
        return ThreadResDto.builder()
                .id(this.id)
                .image(this.workspaceMember.getProfileImage())
                .memberName(this.workspaceMember.getNickname())
                .memberId(this.workspaceMember.getWorkspaceMemberId())
                .createdTime(this.getCreatedTime().toString())
                .content(this.content)
                .files(fileDtos)
                .childThreads(childThreadList)
                .tags(tags)
                .parentThreadId(this.parent != null ? this.parent.getId() : null)
                .canvasId(this.canvasId)
                .canvasTitle(this.canvasTitle)
                .build();
    }

    // 소프트 삭제 메서드
    public void markAsDeleted() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
    }

    public void updateThread(ThreadReqDto threadReqDto) {
        this.content = threadReqDto.getContent();
    }

    public void setThreadFiles(List<ThreadFile> threadFiles) {
        this.threadFiles = threadFiles;
    }
}
