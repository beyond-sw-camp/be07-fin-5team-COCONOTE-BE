package com.example.coconote.api.channel.channelMember.dto.response;

import com.example.coconote.api.channelMember.entity.ChannelRole;
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
    private ChannelRole channelRole;
}
