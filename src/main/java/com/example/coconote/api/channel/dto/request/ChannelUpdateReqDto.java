package com.example.coconote.api.channel.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelUpdateReqDto {
    private String name;
    private String info;
    private boolean isPublic;
}
