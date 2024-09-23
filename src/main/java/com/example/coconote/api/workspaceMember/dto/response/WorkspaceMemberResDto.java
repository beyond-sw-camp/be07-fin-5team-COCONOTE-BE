package com.example.coconote.api.workspaceMember.dto.response;

import com.example.coconote.api.workspaceMember.entity.WsRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberResDto {
    private Long workspaceMemberId;
    private Long workspaceId;
    private Long memberId;
    private String memberName;
    private String nickname;
    private String field;
    private String position;
    private String profileImage;
    private WsRole wsRole;
}
