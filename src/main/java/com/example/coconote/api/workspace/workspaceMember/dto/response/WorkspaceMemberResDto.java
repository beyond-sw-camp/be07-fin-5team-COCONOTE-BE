package com.example.coconote.api.workspace.workspaceMember.dto.response;

import com.example.coconote.api.channel.channelMember.dto.response.ChannelMemberListResDto;
import com.example.coconote.api.workspace.workspaceMember.entity.WsRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<Long> channels;
}
