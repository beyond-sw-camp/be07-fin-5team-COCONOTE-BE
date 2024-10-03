package com.example.coconote.api.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelSearchResultDto {
    private String channelId;
    private String channelName;
    private String channelInfo;
    private Boolean isPublic;
}