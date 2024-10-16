package com.example.coconote.api.channel.channelMember.dto.response;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channelMember.entity.ChannelRole;
import com.example.coconote.api.workspace.workspaceMember.dto.response.WorkspaceMemberResDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMemberListResDto {
    private Long id;
    private Long channelId;
    private ChannelRole channelRole;
    private Boolean isBookmark;
    private WorkspaceMemberResDto memberInfo;


}
