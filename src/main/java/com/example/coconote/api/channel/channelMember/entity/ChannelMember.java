package com.example.coconote.api.channel.channelMember.entity;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channelMember.dto.response.ChannelMemberListResDto;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_member_id")
    private WorkspaceMember workspaceMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ChannelRole channelRole = ChannelRole.USER; // 최초 가입 시에는 그냥 일반 유저

    @Builder.Default
    private Boolean isBookmark = true; // 최초 가입 시에는 즐겨찾기 자동으로 등록돼 있음

    public ChannelMemberListResDto fromEntity() {
        return ChannelMemberListResDto.builder()
                .id(this.id)
                .channelRole(this.channelRole)
                .memberInfo(this.workspaceMember.fromEntity())
                .build();
    }


    public boolean changeRole() { // 권한이 상승(?)했으면 true 반환
        if(this.channelRole.equals(ChannelRole.USER)) {
            this.channelRole = ChannelRole.MANAGER;
            return true;
        }else {
            this.channelRole = ChannelRole.USER;
            return false;
        }
    }

    public boolean bookmarkMyChannel() { // 추가됐으면 true return
        if(this.isBookmark) {
            this.isBookmark = false;
            return false;
        }else {
            this.isBookmark = true;
            return true;
        }
    }

    public void deleteEntity() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
    }
}