package com.example.coconote.api.workspace.workspaceMember.dto.request;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
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
