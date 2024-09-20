package com.example.coconote.api.workspaceMember.entity;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.workspace.entity.Workspace;
import com.example.coconote.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private WsRole wsRole;
}
