package com.example.coconote.api.workspace.workspaceMember.entity;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspaceMember.dto.request.WorkspaceMemberUpdateReqDto;
import com.example.coconote.api.workspace.workspaceMember.dto.response.WorkspaceMemberResDto;
import com.example.coconote.common.BaseEntity;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workspaceMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String memberName;

    private String nickname;

    private String field;

    private String position;

    private String profileImage;

    private Boolean isInvited;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WsRole wsRole = WsRole.USER;


    public WorkspaceMemberResDto fromEntity() {
        return WorkspaceMemberResDto.builder()
                .workspaceMemberId(this.workspaceMemberId)
                .workspaceId(this.workspace.getWorkspaceId())
                .memberId(this.member.getId())
                .memberName(this.memberName)
                .nickname(this.nickname)
                .field(this.field)
                .position(this.position)
                .profileImage(this.profileImage)
                .wsRole(this.wsRole)
                .build();
    }

    public void updateEntity(WorkspaceMemberUpdateReqDto dto) {
        this.memberName = dto.getMemberName();
        this.nickname = dto.getNickname();
        this.field = dto.getField();
        this.position = dto.getPosition();
        this.profileImage = dto.getProfileImage();
    }

    public void changeRole() {
        if(this.wsRole.equals(WsRole.USER)) {
            this.wsRole = WsRole.SMANAGER;
        }else {
            this.wsRole = WsRole.USER;
        }
    }

    public void deleteEntity() {
        this.isDeleted = IsDeleted.Y;
        this.deletedTime = LocalDateTime.now();
    }
}
