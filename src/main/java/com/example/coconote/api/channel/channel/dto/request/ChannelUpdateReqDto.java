package com.example.coconote.api.channel.channel.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelUpdateReqDto {
    private String sectionId;
    private String channelName;
    private String channelInfo;
    private Boolean isPublic;
}
