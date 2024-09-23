package com.example.coconote.api.workspaceMember.dto.request;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.workspace.entity.Workspace;
import com.example.coconote.api.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspaceMember.entity.WsRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberCreateReqDto {
    private Long workspaceId;
    private Long memberId;
    private String memberName;
    private String nickname;
    private String field;
    private String position;
    private String profileImage;

    public WorkspaceMember toEntity(Workspace workspace, Member member) {
        return WorkspaceMember.builder()
                .workspace(workspace)
                .member(member)
                .memberName(this.memberName)
                .nickname(this.nickname)
                .field(this.field)
                .position(this.position)
                .profileImage(this.profileImage)
                .build();
    }
}
