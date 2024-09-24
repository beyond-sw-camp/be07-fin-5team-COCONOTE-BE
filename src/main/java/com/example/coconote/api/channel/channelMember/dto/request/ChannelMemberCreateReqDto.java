package com.example.coconote.api.channel.channelMember.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMemberCreateReqDto {
    private Long workspaceMemberId;
    private Long channelId;
}
