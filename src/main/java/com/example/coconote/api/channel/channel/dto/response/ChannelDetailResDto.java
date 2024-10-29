package com.example.coconote.api.channel.channel.dto.response;

import com.example.coconote.api.channel.channel.entity.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDetailResDto {
    private Long sectionId;
    private Long channelId;
    private String channelName;
    private String channelInfo;
    private Boolean isPublic;
    private ChannelType channelType;
}
