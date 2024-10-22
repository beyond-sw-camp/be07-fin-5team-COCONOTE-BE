package com.example.coconote.api.channel.channelMember.dto.request;

import com.example.coconote.api.channel.channelMember.entity.ChannelRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMemberRoleReqDto {
    private Long id;
    private ChannelRole channelRole;
}
